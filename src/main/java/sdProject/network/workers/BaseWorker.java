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

    private static final int UDP_TIMEOUT_MS = AppConfig.getUdpTimeoutMs();

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
        request.put("serviceName", this.serviceId);
        request.put("serviceType", this.serviceType);

        // Estamos ennviando apenas a porta. O Gateway deve descobrir o IP.
        request.put("servicePort", this.port);

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
            // 1. Executa o registro e obtém a resposta (o "objeto")
            Map<String, Object> response = performRegistration();

            // 2. Processa a resposta
            if (response != null && "success".equals(response.get("status"))) {
                // Opcional: Exibe o IP que o gateway descobriu, se ele for retornado
                String discoveredIp = (String) response.get("discoveredIp");
                System.out.println(workerName + " registrado com sucesso no Gateway Discovery.");
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
            e.printStackTrace();
        }
    }

    protected void sendHeartbeatUDP() {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("operation", "heartbeat");
            request.put("serviceName", serviceId);

            Map<String, Object> response = sendAndReceiveUDP(request);

            if (!"success".equals(response.get("status"))) {
                System.out.println("Falha no heartbeat UDP para " + serviceId + ": " + response.get("message"));
                if ("Serviço não encontrado".equals(response.get("message"))) {
                    registerWithDiscoveryGatewayUDP();
                }
            } else {
                System.out.println("Heartbeat UDP para " + serviceId + " enviado com sucesso.");
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

            // Processa a requisição
            Map<String, Object> response = processRequest(request);

            // Envia a resposta
            connection.send(response);

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
