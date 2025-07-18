package sdProject.network.workers;

import sdProject.config.AppConfig;
import sdProject.network.util.Connection;
import sdProject.network.util.SerializationUtils;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseWorker {
    protected final int port;
    protected final String workerName;
    protected final String serviceId;
    protected final String serviceType; 
    protected final String instanceId; // Novo campo para ID único da instância
    protected volatile boolean running = false;
    protected ServerSocket serverSocket;
    protected final ExecutorService threadPool;
    protected final ScheduledExecutorService heartbeatScheduler;
      private final String gatewayHost;
    private final int gatewayPort;

    private static final int UDP_TIMEOUT_MS = AppConfig.getUdpTimeoutMs();

    // Contador de requisições processadas desde o último heartbeat
    protected final AtomicLong requestCount = new AtomicLong(0);
    protected final AtomicLong lastHeartbeatCount = new AtomicLong(0);

    public BaseWorker(int port, String workerName, String serviceId, int threadPoolSize,
                     String gatewayHost, int gatewayPort) {
        this.port = port;
        this.workerName = workerName;
        this.serviceId = serviceId;
        this.serviceType = determineServiceType(serviceId);
        this.instanceId = generateInstanceId(); // Gera ID único para esta instância
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        this.heartbeatScheduler = Executors.newScheduledThreadPool(1);
        this.gatewayHost = gatewayHost;
        this.gatewayPort = gatewayPort;
    }

    private String determineServiceType(String serviceId) {
        // Extrai o tipo de serviço do ID (ex: "nota-8082-1749941430356" -> "nota")
        if (serviceId.contains("-")) {
            return serviceId.split("-")[0];
        }
        return serviceId;
    }
    
    private String generateInstanceId() {
        // Gera um ID único combinando serviceId, porta e timestamp
        return serviceId + "-" + port + "-" + System.currentTimeMillis();
    }
    
    public BaseWorker(int port, String workerName, String serviceId, int threadPoolSize) {
        this(port, workerName, serviceId, threadPoolSize, AppConfig.getGatewayHost(), AppConfig.getGatewayPort());
    }


    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println(workerName + " iniciado na porta " + port);
            //responsável por lidar com as requisições que chegam, permitindo que o worker processe vários pedidos simultaneamente.
            new Thread(() -> acceptConnections()).start();

            registerWithDiscoveryGatewayUDP();
              // Inicia envio periódico de heartbeats (a cada X segundos conforme configuração)
            heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeatUDP,
                AppConfig.getHeartbeatIntervalSeconds(),
                AppConfig.getHeartbeatIntervalSeconds(),
                TimeUnit.SECONDS);

        } catch (IOException e) {
            System.err.println("Erro ao iniciar " + workerName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> sendAndReceiveUDP(Map<String, Object> requestPayload) throws IOException, ClassNotFoundException {
        try (Connection connection = new Connection()) {
            connection.setSoTimeout(UDP_TIMEOUT_MS);
            InetAddress gatewayAddress = InetAddress.getByName(gatewayHost);

            connection.sendUDP(requestPayload, gatewayAddress, gatewayPort);

            DatagramPacket receivePacket = connection.receiveUDP();
            byte[] actualData = new byte[receivePacket.getLength()];
            System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), actualData, 0, receivePacket.getLength());

            return (Map<String, Object>) SerializationUtils.deserialize(actualData);
        }
    }

    private Map<String, Object> buildRegistrationRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("operation", "register");
        request.put("serviceName", this.serviceType); // Usa serviceType como nome lógico
        request.put("serviceType", this.serviceType);
        request.put("servicePort", this.port);
        request.put("instanceId", this.instanceId); // Garante que instanceId vai no registro
        return request;
    }

    private Map<String, Object> performRegistration() throws IOException, ClassNotFoundException {
        // Monta a requisição sem o IP
        Map<String, Object> requestPayload = buildRegistrationRequest();

        // Envia a requisição e retorna a resposta recebida do Gateway
        System.out.println("Enviando requisição de registro para o Gateway...");
        return sendAndReceiveUDP(requestPayload);
    }



    protected void registerWithDiscoveryGatewayUDP() {
        try {
            String localIP = InetAddress.getLocalHost().getHostAddress();
            String serviceAddress = localIP + ":" + this.port;

            Map<String, Object> request = new HashMap<>();
            request.put("operation", "register");
            request.put("serviceName", serviceType); // Usa serviceType como nome lógico
            request.put("serviceAddress", serviceAddress);
            request.put("serviceType", serviceType);
            request.put("instanceId", instanceId); // Inclui o ID da instância

            // 1. Executa o registro e obtém a resposta (o "objeto")
            Map<String, Object> response = performRegistration();

            // 2. Processa a resposta
            System.out.println("Resposta do Gateway ao registrar: " + response); // Log detalhado
            if (response != null && "success".equals(response.get("status"))) {
                System.out.println(workerName + " registrado no Gateway Discovery (UDP) como " + instanceId);
                // Opcional: Exibe o IP que o gateway descobriu, se ele for retornado
                String discoveredIp = (String) response.get("discoveredIp");
                if (discoveredIp != null) {
                    System.out.println("--> Endereço detectado pelo Gateway: " + discoveredIp + ":" + this.port);
                }
            } else {
                String message = (response != null) ? (String) response.get("message") : "Nenhuma resposta recebida.";
                System.err.println("Falha ao registrar no Gateway (UDP): " + message);
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout ao registrar no Gateway Discovery (UDP): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao registrar no Gateway Discovery (UDP): " + e.getMessage());
        }
    }

    // Pode ser sobrescrito por subclasses para enviar métricas reais
    protected Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        long current = requestCount.get();
        long last = lastHeartbeatCount.getAndSet(current);
        long sinceLastHeartbeat = current - last;
        metrics.put("requestsSinceLastHeartbeat", sinceLastHeartbeat); // Apenas essa métrica
        return metrics;
    }

    protected void sendHeartbeatUDP() {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("operation", "heartbeat");
            request.put("serviceName", serviceType); // Usa serviceType como nome lógico
            request.put("instanceId", instanceId); // Inclui o ID da instância
            request.put("metrics", getMetrics()); // Envia métricas

            Map<String, Object> response = sendAndReceiveUDP(request);

            System.out.println("[HEARTBEAT] instanceId=" + instanceId + ", resposta=" + response); // Log detalhado

            if (!"success".equals(response.get("status"))) {
                System.out.println("Falha no heartbeat UDP para " + instanceId + ": " + response.get("message"));
                if ("Serviço não encontrado".equals(response.get("message"))) {
                    registerWithDiscoveryGatewayUDP();
                }
            } else {
                System.out.println("Heartbeat UDP para " + instanceId + " enviado com sucesso.");
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout no heartbeat para Gateway Discovery (UDP): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao enviar heartbeat para Gateway Discovery (UDP): " + e.getMessage());
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


    @SuppressWarnings("unchecked")
    protected void handleClient(Socket clientSocket) {
        try (Connection connection = new Connection(clientSocket)) {
            // Lê a requisição do cliente
            Map<String, Object> request = (Map<String, Object>) connection.receive();

            // Incrementa o contador de requisições
            requestCount.incrementAndGet();

            // Processa a requisição
            Map<String, Object> response = processRequest(request);

            // Envia a resposta
            connection.send(response);

        } catch (EOFException eof) {
            System.err.println("[INFO] Conexão fechada pelo cliente antes de enviar dados (EOFException). IP: " + clientSocket.getInetAddress().getHostAddress());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao processar cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }


    protected abstract Map<String, Object> processRequest(Map<String, Object> request);

    protected Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return response;
    }

    protected Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }

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
    
    
}