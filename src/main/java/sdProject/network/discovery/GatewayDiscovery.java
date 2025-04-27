package sdProject.network.discovery;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.ConnectException;
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
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final ScheduledExecutorService healthChecker;
    

    public GatewayDiscovery(int port, int threadPoolSize) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        this.healthChecker = Executors.newScheduledThreadPool(1);
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Gateway Discovery iniciado na porta " + port);
            
            new Thread(() -> acceptConnections()).start();
            
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
            if (currentTime - info.lastHeartbeat > 30000) {
                System.out.println("Serviço inativo detectado: " + serviceName + " em " + info.address);
                
                // Tenta conectar para confirmar se realmente está inativo
                if (!isServiceAlive(info.address)) {
                    System.out.println("Confirmado: Serviço " + serviceName + " está inativo. Removendo do registro.");
                    serviceRegistry.remove(serviceName);
                }
            }
        });
    }
    
    private boolean isServiceAlive(String address) {
        try {
            String[] parts = address.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            Socket socket = new Socket(host, port);
            socket.close();
            return true;
        } catch (ConnectException e) {
            return false;
        } catch (Exception e) {
            System.err.println("Erro ao verificar serviço: " + e.getMessage());
            return false;
        }
    }
    
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão aceita de: " + clientSocket.getInetAddress().getHostAddress());
                
                threadPool.submit(() -> handleClient(clientSocket));
                
            } catch (IOException e) {
                if (running) { 
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            
            Map<String, Object> request = (Map<String, Object>) in.readObject();
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
            
            out.writeObject(response);
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao processar cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar socket: " + e.getMessage());
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
    }

    public void stop() {
        running = false;
        threadPool.shutdown();
        healthChecker.shutdown();
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("Gateway Discovery parado");
        } catch (IOException e) {
            System.err.println("Erro ao parar Gateway Discovery: " + e.getMessage());
        }
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
