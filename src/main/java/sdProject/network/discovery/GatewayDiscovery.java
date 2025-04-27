package sdProject.network.discovery;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GatewayDiscovery {
    private final int port;
    
    private final Map<String, String> serviceRegistry = new ConcurrentHashMap<>();
    
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    

    public GatewayDiscovery(int port, int threadPoolSize) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Gateway Discovery iniciado na porta " + port);
            
            new Thread(() -> acceptConnections()).start();
            
        } catch (IOException e) {
            System.err.println("Erro ao iniciar Gateway Discovery: " + e.getMessage());
            e.printStackTrace();
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
        
        Map<String, Object> response = new HashMap<>();
        
        if (serviceName == null || serviceAddress == null) {
            response.put("status", "error");
            response.put("message", "serviceName e serviceAddress são obrigatórios");
            return response;
        }
        
        serviceRegistry.put(serviceName, serviceAddress);
        System.out.println("Serviço registrado: " + serviceName + " em " + serviceAddress);
        
        response.put("status", "success");
        response.put("message", "Serviço registrado com sucesso");
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
        
        String serviceAddress = serviceRegistry.get(serviceName);
        
        if (serviceAddress == null) {
            response.put("status", "error");
            response.put("message", "Serviço não encontrado: " + serviceName);
            return response;
        }
        
        response.put("status", "success");
        response.put("serviceAddress", serviceAddress);
        return response;
    }

    public void stop() {
        running = false;
        threadPool.shutdown();
        
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
