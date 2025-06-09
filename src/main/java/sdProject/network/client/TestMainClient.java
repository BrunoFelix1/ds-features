package sdProject.network.client;

import sdProject.config.AppConfig;
import sdProject.network.util.Connection;
import sdProject.network.util.SerializationUtils;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestMainClient {
    private static final String MATRICULA_SERVICE = "matricula";
    private static final String NOTA_SERVICE = "nota";
    private static final String HISTORICO_SERVICE = "historico";
    
    private final String gatewayHost;
    private final int gatewayPort;      // Cache de localização dos serviços (serviço -> endereço:porta)
    private final Map<String, ServiceLocation> serviceCache = new ConcurrentHashMap<>();
    
    private static final int UDP_TIMEOUT_MS = AppConfig.getUdpTimeoutMs();

    public TestMainClient(String gatewayHost, int gatewayPort) {
        this.gatewayHost = gatewayHost; // ISSO AQUI É O UNICO IP DA APLICAÇÃO BASICAMENTE, é setado nas mains dos serviços
        this.gatewayPort = gatewayPort;
    }@SuppressWarnings("unchecked") // é so pra nao aparecer o erro do cast de objeto para map ali no retorno
    private Map<String, Object> sendToGatewayUDP(Map<String, Object> requestPayload) throws IOException, ClassNotFoundException {
        try (Connection connection = new Connection()) {
            connection.setSoTimeout(UDP_TIMEOUT_MS);
            InetAddress gwAddress = InetAddress.getByName(gatewayHost);
            
            connection.sendUDP(requestPayload, gwAddress, gatewayPort);
            DatagramPacket receivePacket = connection.receiveUDP();
            
            byte[] actualData = new byte[receivePacket.getLength()];
            System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), actualData, 0, receivePacket.getLength());
            
            return (Map<String, Object>) SerializationUtils.deserialize(actualData);
        }
    }
      private ServiceLocation discoverService(String serviceName) throws IOException, ClassNotFoundException {
        ServiceLocation cachedLocation = serviceCache.get(serviceName);
        if (cachedLocation != null) {
            // disponibilidae dele aqui
            try (Connection testConnection = new Connection(cachedLocation.getHost(), cachedLocation.getPort())) {
                return cachedLocation;  // Serviço ainda está ativo
            } catch (ConnectException e) {
                // Serviço não está disponível, entao a gnt remove ele
                serviceCache.remove(serviceName);
                System.out.println("Serviço " + serviceName + " não está mais disponível.");
            }
        }
        
        Map<String, Object> request = new HashMap<>();
        request.put("operation", "discover");
        request.put("serviceName", serviceName);

        try {
            Map<String, Object> response = sendToGatewayUDP(request);
            if ("success".equals(response.get("status"))){
                String serviceAddress = (String) response.get("serviceAddress"); // Aqui recebemos o enderço TCP do Worker
                ServiceLocation location = new ServiceLocation(serviceAddress);
                serviceCache.put(serviceName, location);
                System.out.println("Serviço " + serviceName + " descoberto no endereço " + serviceAddress);
                return location;
            } else {
                System.err.println("Falha ao descobrir serviço " + serviceName + " " + response.get("message"));
                return findAlternativeService(serviceName);
            }
        } catch (SocketTimeoutException e){
            System.err.println("Timeout ao descobrir serviço " + serviceName + " do Gateway UDP.");
            return findAlternativeService(serviceName);
        }
    }
    
    @SuppressWarnings("unchecked")
    private ServiceLocation findAlternativeService(String serviceType) throws IOException, ClassNotFoundException {
        System.out.println("Procurando serviços alternativos para " + serviceType);
        
        Map<String, Object> request = new HashMap<>();
        request.put("operation", "getServices");
        request.put("serviceType", serviceType);

        try {
            Map<String, Object> response = sendToGatewayUDP(request);
            if ("success".equals(response.get("status"))){
                Map<String, String> services = (Map<String, String>) response.get("services");
                if (services != null && !services.isEmpty()){
                    // Aqui pega o primeiro serviço alternativo encontrado
                   Map.Entry<String, String> entry = services.entrySet().iterator().next();
                   String altServiceName = entry.getKey();
                   String serviceAddress = entry.getValue(); // Endereço TCP do worker

                    ServiceLocation location = new ServiceLocation(serviceAddress);
                    serviceCache.put(serviceType, location);
                    System.out.println("Usando serviço alternativo " + altServiceName + " em " + serviceAddress + " para tipo " + serviceType);
                    return location;
                }
            }
            System.err.println("Gateway não retornou serviços alternativos para " + serviceType + response.get("message"));
        } catch (SocketTimeoutException e){
            System.err.println("Timeout ao buscar serviços alternativos no Gateway para " + serviceType);
        }
        throw new IOException("Nenhum serviço do tipo " + serviceType + " disponível via Gateway.");
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> callService(String serviceName, Map<String, Object> request) 
            throws IOException, ClassNotFoundException {
        
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                ServiceLocation location = discoverService(serviceName);
                  try (Connection connection = new Connection(location.getHost(), location.getPort())) {
                    connection.send(request);
                    return (Map<String, Object>) connection.receive();
                } catch (ConnectException e) {
                    serviceCache.remove(serviceName);
                    System.out.println("Falha na conexão com " + serviceName + ". Tentando novamente...");
                    retryCount++;
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro ao chamar serviço " + serviceName + ": " + e.getMessage());
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw e;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        throw new IOException("Falha após " + maxRetries + " tentativas de chamar o serviço " + serviceName);
    }


    // ESSAS FUNCOES AQUI SÃO EXEMPLOS DE COMO CHAMAR OS SERVIÇOS, A GENTE PODE MUDAR DEPOIS
    // MAS O IMPORTANTE É QUE ELAS CHAMAM O GATEWAY E RETORNAM O RESULTADO
    // E MOSTRAM AS CARACTERISISTICAS DE SD
    public Map<String, Object> matricularAluno(int alunoId, int disciplinaId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "matricular");
        request.put("alunoId", alunoId);
        request.put("disciplinaId", disciplinaId);
        
        try {
            return callService(MATRICULA_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao matricular aluno: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }
    
    public Map<String, Object> registrarNota(int alunoId, int disciplinaId, Double nota) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "registrarNota");
        request.put("alunoId", alunoId);
        request.put("disciplinaId", disciplinaId);
        request.put("nota", nota);
        
        try {
            return callService(NOTA_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao registrar nota: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }
    
    public Map<String, Object> gerarHistoricoCompleto(int alunoId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "historicoCompleto");
        request.put("alunoId", alunoId);
        
        try {
            return callService(HISTORICO_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao gerar histórico: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }
        // Só rodar para testar o cliente
    public static void main(String[] args) {
        // essa é a unica definicao de ip da aplicação basicamente, o resto
        // é tudo dinâmico, a partir do gateway discovery
        String gatewayHost = AppConfig.getGatewayHost();
        int gatewayPort = AppConfig.getGatewayPort();
        
        // Verificar se host e porta foram passados como argumentos (sobrescreve as configurações)
        if (args.length >= 2) {
            gatewayHost = args[0];
            gatewayPort = Integer.parseInt(args[1]);
        }
        
        TestMainClient client = new TestMainClient(gatewayHost, gatewayPort);
        
        // Exemplo de uso do cliente abaixo, a gente modifica dps
        try {
            // Matricular aluno
            Map<String, Object> matriculaResponse = client.matricularAluno(1, 1);
            System.out.println("Resposta da matrícula: " + matriculaResponse);
            
            // Registrar nota
            Map<String, Object> notaResponse = client.registrarNota(1, 1, 8.5);
            System.out.println("Resposta do registro de nota: " + notaResponse);
            
            // Gerar histórico
            Map<String, Object> historicoResponse = client.gerarHistoricoCompleto(1);
            System.out.println("Resposta do histórico: " + historicoResponse);
            
        } catch (Exception e) {
            System.err.println("Erro ao testar cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
