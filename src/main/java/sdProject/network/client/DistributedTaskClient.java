package sdProject.network.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class DistributedTaskClient {
    private static final String MATRICULA_SERVICE = "matricula";
    private static final String NOTA_SERVICE = "nota";
    private static final String HISTORICO_SERVICE = "historico";
    
    private final String gatewayHost;
    private final int gatewayPort;
    
    // Cache de localização dos serviços (serviço -> endereço:porta)
    private final Map<String, ServiceLocation> serviceCache = new HashMap<>();
    
    public DistributedTaskClient(String gatewayHost, int gatewayPort) {
        this.gatewayHost = gatewayHost;
        this.gatewayPort = gatewayPort;
    }
    
    private ServiceLocation discoverService(String serviceName) throws IOException, ClassNotFoundException {
        // Verifica se já temos o serviço em cache
        if (serviceCache.containsKey(serviceName)) {
            return serviceCache.get(serviceName);
        }
        
        Map<String, Object> request = new HashMap<>();
        request.put("operation", "discover");
        request.put("serviceName", serviceName);
        
        try (Socket socket = new Socket(gatewayHost, gatewayPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject(request);
            
            Map<String, Object> response = (Map<String, Object>) in.readObject();
            
            if ("success".equals(response.get("status"))) {
                String serviceAddress = (String) response.get("serviceAddress");
                ServiceLocation location = new ServiceLocation(serviceAddress);
                
                serviceCache.put(serviceName, location);
                
                System.out.println("Serviço " + serviceName + " descoberto em " + serviceAddress);
                return location;
            } else {
                throw new IOException("Erro ao descobrir serviço: " + response.get("message"));
            }
        }
    }
    
    public Map<String, Object> callService(String serviceName, Map<String, Object> request) 
            throws IOException, ClassNotFoundException {
        
        ServiceLocation location = discoverService(serviceName);
        
        try (Socket socket = new Socket(location.getHost(), location.getPort());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject(request);
            
            return (Map<String, Object>) in.readObject();
        } catch (IOException e) {
            serviceCache.remove(serviceName);
            throw e;
        }
    }

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
        String gatewayHost = "localhost";
        int gatewayPort = 8080;
        
        // Verificar se host e porta foram passados como argumentos
        if (args.length >= 2) {
            gatewayHost = args[0];
            gatewayPort = Integer.parseInt(args[1]);
        }
        
        DistributedTaskClient client = new DistributedTaskClient(gatewayHost, gatewayPort);
        
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
