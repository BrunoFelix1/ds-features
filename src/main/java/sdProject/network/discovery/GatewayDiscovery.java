package sdProject.network.discovery;

import sdProject.network.util.Connection;
import sdProject.network.util.SerializationUtils;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GatewayDiscovery {
    private final int port;
    
    private final Map<String, ServiceInfo> serviceRegistry = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    private Connection connection;
    private final ExecutorService threadPool;
    private final ScheduledExecutorService healthChecker;

    
    public GatewayDiscovery(int port, int threadPoolSize) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        this.healthChecker = Executors.newScheduledThreadPool(1);
    }
      public void start() {
        try {
            connection = new Connection(port);
            running = true;
            System.out.println("Gateway Discovery (UDP) iniciado na porta " + port);

            // Inicia a thread para receber pacotes UDP
            new Thread(this::receivePackets).start();
            
            // Inicia verificação periódica da saúde dos serviços
            healthChecker.scheduleAtFixedRate(this::checkServiceHealth, 10, 10, TimeUnit.SECONDS);
            
        } catch (IOException e) {
            System.err.println("Erro ao iniciar Gateway Discovery: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void checkServiceHealth() {
        System.out.println("Verificando saúde dos serviços registrados...");
        long currentTime = System.currentTimeMillis();
        
        serviceRegistry.forEach((serviceName, info) -> {
            // Considera um serviço inativo se não tiver enviado heartbeat nos últimos 30 segundos
            if (currentTime - info.lastHeartbeat > 15000) {
                System.out.println("Serviço inativo detectado: " + serviceName + " em " + info.address);
                
                // Tenta conectar para confirmar se realmente está inativo
                if (!isServiceAlive(info.address)) {
                    System.out.println("Confirmado: Serviço " + serviceName + " está inativo. Removendo do registro.");
                    serviceRegistry.remove(serviceName);
                } else {
                    if (currentTime - info.lastHeartbeat > 60000){
                        System.out.println("Removendo " + serviceName + " devido a heartbeats UDP ausentes prolongados, apesar de TCP check OK.");
                        serviceRegistry.remove(serviceName);
                    }
                }
            }
        });
    }
      private boolean isServiceAlive(String address) {
        try {
            String[] parts = address.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            try (Connection connection = new Connection(host, port)) {
                return true; // Se conseguiu conectar, o serviço está vivo
            }
        } catch (ConnectException e) {
            return false;
        } catch (Exception e) {
            System.err.println("Erro ao verificar serviço: " + e.getMessage());
            return false;
        }
    }
      private void receivePackets() {
        while (running) {
            try {
                DatagramPacket packet = connection.receiveUDP();
                byte[] receiveData = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), receiveData, 0, packet.getLength());

                threadPool.submit(() -> handlePacket(receiveData, packet.getAddress(), packet.getPort()));
                
            } catch (IOException e) {
                if (running) { 
                    System.err.println("Erro ao receber pacote UDP: " + e.getMessage());
                }
            }
        }
    }
    
    private void handlePacket(byte[] data, InetAddress clientAddress, int clientPort) {
        try {
            
            Map<String, Object> request = (Map<String, Object>) SerializationUtils.deserialize(data);
            String operation = (String) request.get("operation");
            
            Map<String, Object> response = new HashMap<>();
            
            switch (operation) {
                case "register":
                    response = registerService(request);
                    break;
                case "discover":
                    response = discoverService(request);
                    break;
                case "heartbeat":
                    response = handleHeartbeat(request);
                    break;
                case "getServices":
                    response = getServicesOfType(request);
                    break;
                default:
                    response.put("status", "error");
                    response.put("message", "Operação desconhecida: " + operation);
            }
              connection.sendUDP(response, clientAddress, clientPort);
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao processar pacote UDP: " + e.getMessage());
            e.printStackTrace();            try {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Erro interno no gateway: " + e.getMessage());
                connection.sendUDP(errorResponse, clientAddress, clientPort);
            } catch (IOException ex){
                System.err.println("Erro ao enviar resposta de erro UDP: " + ex.getMessage());
            }
        }
    }
    
    private Map<String, Object> registerService(Map<String, Object> request) {
        String serviceName = (String) request.get("serviceName");
        String serviceAddress = (String) request.get("serviceAddress");
        String serviceType = (String) request.get("serviceType");
        
        Map<String, Object> response = new HashMap<>();
        
        if (serviceName == null || serviceAddress == null || serviceType == null) {
            response.put("status", "error");
            response.put("message", "serviceName, serviceAddress e serviceType são obrigatórios");
            return response;
        }
        
        serviceRegistry.put(serviceName, new ServiceInfo(serviceAddress, serviceType));
        System.out.println("Serviço registrado: " + serviceName + " (" + serviceType + ") em " + serviceAddress);
        
        response.put("status", "success");
        response.put("message", "Serviço registrado com sucesso");
        return response;
    }
    
    private Map<String, Object> handleHeartbeat(Map<String, Object> request) {
        String serviceName = (String) request.get("serviceName");
        Map<String, Object> response = new HashMap<>();
        
        if (serviceName == null) {
            response.put("status", "error");
            response.put("message", "serviceName é obrigatório");
            return response;
        }
        
        ServiceInfo info = serviceRegistry.get(serviceName);
        if (info != null) {
            info.lastHeartbeat = System.currentTimeMillis();
            response.put("status", "success");
        } else {
            response.put("status", "error");
            response.put("message", "Serviço não encontrado: " + serviceName);
        }
        
        return response;
    }
    
    private Map<String, Object> getServicesOfType(Map<String, Object> request) {
        String serviceType = (String) request.get("serviceType");
        Map<String, Object> response = new HashMap<>();
        
        if (serviceType == null) {
            response.put("status", "error");
            response.put("message", "serviceType é obrigatório");
            return response;
        }
        
        Map<String, String> services = new HashMap<>();
        serviceRegistry.forEach((name, info) -> {
            if (serviceType.equals(info.serviceType)) {
                services.put(name, info.address);
            }
        });
        
        response.put("status", "success");
        response.put("services", services);
        return response;
    }
    
    private Map<String, Object> discoverService(Map<String, Object> request) {
        String serviceName = (String) request.get("serviceName");
        
        Map<String, Object> response = new HashMap<>();
        
        if (serviceName == null) {
            response.put("status", "error");
            response.put("message", "serviceName é obrigatório");
            return response;
        }
        
        ServiceInfo info = serviceRegistry.get(serviceName);
        
        if (info == null) {
            // Se o serviço exato não foi encontrado, procuramos outros do mesmo tipo
            String serviceType = serviceName; // Assume que o nome é o tipo
            
            for (Map.Entry<String, ServiceInfo> entry : serviceRegistry.entrySet()) {
                if (serviceType.equals(entry.getValue().serviceType)) {
                    response.put("status", "success");
                    response.put("serviceAddress", entry.getValue().address);
                    return response;
                }
            }
            
            response.put("status", "error");
            response.put("message", "Serviço não encontrado: " + serviceName);
            return response;
        }
        
        response.put("status", "success");
        response.put("serviceAddress", info.address);
        return response;
    }    public void stop() {
        running = false;
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }

        threadPool.shutdown();
        healthChecker.shutdown();
        System.out.println("Gateway Discovery (UDP) parado");
    }
    
    public static void main(String[] args) {
        try {
            int port = 8080; 
            
            // Verificar se a porta foi passada como argumento
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
            
            GatewayDiscovery gateway = new GatewayDiscovery(port, 10);
            gateway.start();
            
            // Adiciona shutdown hook para parar o gateway quando o JVM for encerrado
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Parando Gateway Discovery...");
                gateway.stop();
            }));
            
        } catch (Exception e) {
            System.err.println("Erro ao iniciar Gateway Discovery: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
