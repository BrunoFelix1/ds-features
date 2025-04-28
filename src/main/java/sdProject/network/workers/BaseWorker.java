package sdProject.network.workers;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class BaseWorker {
    protected final int port;
    protected final String workerName;
    protected final String serviceId;
    protected final String serviceType; 
    protected volatile boolean running = false;
    protected ServerSocket serverSocket;
    protected final ExecutorService threadPool;
    protected final ScheduledExecutorService heartbeatScheduler;
    
    private final String gatewayHost;
    private final int gatewayPort;
    
    public BaseWorker(int port, String workerName, String serviceId, int threadPoolSize, 
                     String gatewayHost, int gatewayPort) {
        this.port = port;
        this.workerName = workerName;
        this.serviceId = serviceId;
        this.serviceType = determineServiceType(serviceId);
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        this.heartbeatScheduler = Executors.newScheduledThreadPool(1);
        this.gatewayHost = gatewayHost;
        this.gatewayPort = gatewayPort;
    }
    
    private String determineServiceType(String serviceId) {
        // Extrai o tipo de serviço (nota, matricula, historico) do ID
        // Se o formato for diferente, ajuste conforme necessário
        return serviceId;
    }
    
    public BaseWorker(int port, String workerName, String serviceId, int threadPoolSize) {
        this(port, workerName, serviceId, threadPoolSize, "localhost", 8080);
    }
    

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println(workerName + " iniciado na porta " + port);
            
            new Thread(() -> acceptConnections()).start();
            
            registerWithDiscoveryGateway();
            
            // Inicia envio periódico de heartbeats (a cada 10 segundos)
            heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeat, 10, 10, TimeUnit.SECONDS);
            
        } catch (IOException e) {
            System.err.println("Erro ao iniciar " + workerName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    protected void registerWithDiscoveryGateway() {
        try {
            String localIP = InetAddress.getLocalHost().getHostAddress();
            String serviceAddress = localIP + ":" + port;
            
            Map<String, Object> request = new HashMap<>();
            request.put("operation", "register");
            request.put("serviceName", serviceId);
            request.put("serviceAddress", serviceAddress);
            request.put("serviceType", serviceType);
            
            try (Socket socket = new Socket(gatewayHost, gatewayPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                out.writeObject(request);
                
                Map<String, Object> response = (Map<String, Object>) in.readObject();
                
                if ("success".equals(response.get("status"))) {
                    System.out.println(workerName + " registrado no Gateway Discovery como " + serviceId);
                } else {
                    System.out.println("Falha ao registrar no Gateway: " + response.get("message"));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao registrar no Gateway Discovery: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    protected void sendHeartbeat() {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("operation", "heartbeat");
            request.put("serviceName", serviceId);
            
            try (Socket socket = new Socket(gatewayHost, gatewayPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                out.writeObject(request);
                Map<String, Object> response = (Map<String, Object>) in.readObject();
                
                if (!"success".equals(response.get("status"))) {
                    System.out.println("Falha no heartbeat para " + serviceId + ": " + response.get("message"));
                    // Tenta se registrar novamente
                    registerWithDiscoveryGateway();
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao enviar heartbeat para Gateway Discovery: " + e.getMessage());
            // Não empilha o erro para evitar logs excessivos
        }
    }
    
    protected void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão aceita de: " + clientSocket.getInetAddress().getHostAddress());
                
                // Enviar conexão para ser processada pelo pool de threads
                threadPool.submit(() -> handleClient(clientSocket));
                
            } catch (IOException e) {
                if (running) { // Só loga erro se o worker ainda estiver rodando
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }
    }
    

    protected void handleClient(Socket clientSocket) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            
            // Lê a requisição do cliente
            Map<String, Object> request = (Map<String, Object>) in.readObject();
            
            // Processa a requisição
            Map<String, Object> response = processRequest(request);
            
            // Envia a resposta
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
    

    protected abstract Map<String, Object> processRequest(Map<String, Object> request);
    

    // Esse aí para o worker
    public void stop() {
        running = false;
        threadPool.shutdown();
        heartbeatScheduler.shutdown();
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println(workerName + " parado");
        } catch (IOException e) {
            System.err.println("Erro ao parar " + workerName + ": " + e.getMessage());
        }
    }
    
    protected Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }
}
