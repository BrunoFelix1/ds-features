package sdProject.network.discovery;

import sdProject.network.util.Connection;
import sdProject.network.util.SerializationUtils;
import sdProject.config.AppConfig;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

public class GatewayDiscovery {
    private final int port;
    
    // Mudança: agora cada serviço pode ter múltiplas instâncias
    private final Map<String, List<ServiceInfo>> serviceRegistry = new ConcurrentHashMap<>();
    // Para load balancing round-robin
    private final Map<String, AtomicInteger> roundRobinCounters = new ConcurrentHashMap<>();
    
    private volatile boolean running = false;
    private Connection connection;
    private final ExecutorService threadPool;
    private final ScheduledExecutorService healthChecker;
    public GatewayDiscovery(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(AppConfig.getGatewayThreadPoolSize());
        this.healthChecker = Executors.newScheduledThreadPool(1);
    }
      public void start() {
        final String GREEN = "\u001B[32m";
        @SuppressWarnings("unused")
        final String YELLOW = "\u001B[33m";
        final String RED = "\u001B[31m";
        final String RESET = "\u001B[0m";
        try {
            connection = new Connection(port);
            running = true;
            System.out.println(GREEN + "Gateway Discovery (UDP) iniciado na porta " + port + RESET);

            // Inicia a thread para receber pacotes UDP
            new Thread(this::receivePackets).start();
              // Inicia verificação periódica da saúde dos serviços
            healthChecker.scheduleAtFixedRate(this::checkServiceHealth, 
                AppConfig.getHealthCheckIntervalSeconds(), 
                AppConfig.getHealthCheckIntervalSeconds(), 
                TimeUnit.SECONDS);
            
        } catch (IOException e) {
            System.err.println(RED + "Erro ao iniciar Gateway Discovery: " + e.getMessage() + RESET);
            e.printStackTrace();
        }
    }
    
    private void checkServiceHealth() {
        final String YELLOW = "\u001B[33m";
        final String RESET = "\u001B[0m";
        System.out.println(YELLOW + "Verificando saúde dos serviços registrados..." + RESET);
        long currentTime = System.currentTimeMillis();
        
        serviceRegistry.forEach((serviceType, instances) -> {
            instances.removeIf(info -> {
                if (currentTime - info.lastHeartbeat > 15000) {
                    System.out.println("Serviço inativo detectado: " + info.instanceId + " em " + info.address);
                    
                    if (!isServiceAlive(info.address)) {
                        System.out.println("Confirmado: Instância " + info.instanceId + " está inativa. Removendo do registro.");
                        return true;
                    } else {
                        if (currentTime - info.lastHeartbeat > 60000) {
                            System.out.println("Removendo " + info.instanceId + " devido a heartbeats UDP ausentes prolongados.");
                            return true;
                        }
                    }
                }
                return false;
            });
            
            // Remove listas vazias
            if (instances.isEmpty()) {
                serviceRegistry.remove(serviceType);
                roundRobinCounters.remove(serviceType);
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
    
    @SuppressWarnings("unchecked")
    private void handlePacket(byte[] data, InetAddress clientAddress, int clientPort) {
        try {
            
            Map<String, Object> request = (Map<String, Object>) SerializationUtils.deserialize(data);
            String operation = (String) request.get("operation");
            
            Map<String, Object> response = new HashMap<>();
            
            switch (operation) {
                case "register":
                    response = registerService(request, clientAddress);
                    break;
                case "discover":
                    response = discoverService(request);
                    break;
                case "heartbeat":
                    response = handleHeartbeat(request);
                    break;
                case "getServiceStatus":
                    response = getServiceStatus(request);
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
    
    private Map<String, Object> registerService(Map<String, Object> request, InetAddress clientAddress) {
        String serviceName = (String) request.get("serviceName");
        Integer servicePort = (Integer) request.get("servicePort");
        String serviceType = (String) request.get("serviceType");
        String instanceId = (String) request.get("instanceId"); // Novo campo para ID único da instância
        
        Map<String, Object> response = new HashMap<>();
        
        if (serviceName == null || servicePort == null || serviceType == null) {
            response.put("status", "error");
            response.put("message", "serviceName, servicePort e serviceType são obrigatórios");
            return response;
        }

        String clientIp = clientAddress.getHostAddress();
        String constructedAddress = clientIp + ":" + servicePort;
        
        // Se não foi fornecido instanceId, usar serviceName como fallback
        if (instanceId == null) {
            instanceId = serviceName;
        }
        
        // Adiciona a instância à lista do tipo de serviço
        serviceRegistry.computeIfAbsent(serviceType, k -> new ArrayList<>())
                     .add(new ServiceInfo(constructedAddress, serviceType, instanceId));
        
        // Inicializa contador round-robin se necessário
        roundRobinCounters.putIfAbsent(serviceType, new AtomicInteger(0));
        
        final String GREEN = "\u001B[32m";
        final String RESET = "\u001B[0m";
        System.out.println(GREEN + "Instância registrada: " + instanceId + " (" + serviceType + ") em " + constructedAddress + RESET);
        
        response.put("status", "success");
        response.put("message", "Instância registrada com sucesso");
        response.put("discoveredIp", clientIp);
        return response;
    }
    
    private Map<String, Object> handleHeartbeat(Map<String, Object> request) {
        String serviceName = (String) request.get("serviceName");
        String instanceId = (String) request.get("instanceId");
        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) request.get("metrics"); // Novo: métricas
        Map<String, Object> response = new HashMap<>();
        
        if (serviceName == null) {
            response.put("status", "error");
            response.put("message", "serviceName é obrigatório");
            return response;
        }
        
        // Se não foi fornecido instanceId, usar serviceName
        if (instanceId == null) {
            instanceId = serviceName;
        }
        
        boolean found = false;
        // Procura em todos os tipos de serviço pela instância específica
        for (List<ServiceInfo> instances : serviceRegistry.values()) {
            for (ServiceInfo info : instances) {
                if (instanceId.equals(info.instanceId)) {
                    info.lastHeartbeat = System.currentTimeMillis();
                    if (metrics != null) {
                        info.metrics = metrics;
                        // Adiciona cor amarela ao texto MONITORAMENTO[]
                        String yellow = "\u001B[33m";
                        String reset = "\u001B[0m";
                        System.out.println(yellow + "[MONITORAMENTO]" + reset + " Heartbeat de " + info.instanceId + " (" + info.serviceType + ") métricas: " + metrics);
                    }
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        
        if (found) {
            response.put("status", "success");
        } else {
            response.put("status", "error");
            response.put("message", "Serviço não encontrado: " + instanceId);
        }
        
        return response;
    }

    // Novo: retorna status/monitoramento de todas as instâncias de um tipo de serviço
    private Map<String, Object> getServiceStatus(Map<String, Object> request) {
        String serviceType = (String) request.get("serviceType");
        Map<String, Object> response = new HashMap<>();
        if (serviceType == null) {
            response.put("status", "error");
            response.put("message", "serviceType é obrigatório");
            return response;
        }
        List<ServiceInfo> instances = serviceRegistry.get(serviceType);
        Map<String, Object> status = new HashMap<>();
        if (instances != null) {
            for (ServiceInfo info : instances) {
                Map<String, Object> inst = new HashMap<>();
                inst.put("address", info.address);
                inst.put("lastHeartbeat", info.lastHeartbeat);
                inst.put("metrics", info.metrics);
                status.put(info.instanceId, inst);
            }
        }
        response.put("status", "success");
        response.put("instances", status);
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
        List<ServiceInfo> instances = serviceRegistry.get(serviceType);
        
        if (instances != null) {
            for (ServiceInfo info : instances) {
                services.put(info.instanceId, info.address);
            }
        }
        
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
        
        // Primeiro tenta encontrar por instanceId específico
        for (List<ServiceInfo> instances : serviceRegistry.values()) {
            for (ServiceInfo info : instances) {
                if (serviceName.equals(info.instanceId)) {
                    response.put("status", "success");
                    response.put("serviceAddress", info.address);
                    return response;
                }
            }
        }
        
        // Se não encontrou instância específica, procura por tipo de serviço com load balancing
        List<ServiceInfo> instances = serviceRegistry.get(serviceName);
        if (instances != null && !instances.isEmpty()) {
            // Load balancing round-robin
            AtomicInteger counter = roundRobinCounters.get(serviceName);
            if (counter == null) {
                counter = new AtomicInteger(0);
                roundRobinCounters.put(serviceName, counter);
            }
            
            int index = counter.getAndIncrement() % instances.size();
            ServiceInfo selectedInstance = instances.get(index);
            
            response.put("status", "success");
            response.put("serviceAddress", selectedInstance.address);
            response.put("selectedInstance", selectedInstance.instanceId);
            return response;
        }
        
        response.put("status", "error");
        response.put("message", "Serviço não encontrado: " + serviceName);
        return response;
    }    public void stop() {
        final String YELLOW = "\u001B[33m";
        final String RESET = "\u001B[0m";
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
        System.out.println(YELLOW + "Gateway Discovery (UDP) parado" + RESET);
    }
      public static void main(String[] args) {
        try {
            int port = AppConfig.getGatewayPort(); 
            
            // Verificar se a porta foi passada como argumento
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
            
            GatewayDiscovery gateway = new GatewayDiscovery(port);
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
