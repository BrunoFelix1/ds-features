package sdProject.network.workers.monitor;

import sdProject.network.util.SerializationUtils;
import sdProject.network.util.Connection;
import sdProject.config.AppConfig;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class WorkerMonitor {
    private final String gatewayHost;
    private final int gatewayPort;
    private final ScheduledExecutorService scheduler;
    private final Map<String, WorkerInfo> workerConfigs;
    private final List<String> remoteAgents;

    private static final int UDP_TIMEOUT_MS = AppConfig.getUdpTimeoutMs();
    
    public WorkerMonitor(String gatewayHost, int gatewayPort) {
        this.gatewayHost = gatewayHost;
        this.gatewayPort = gatewayPort;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.workerConfigs = new HashMap<>();
        this.remoteAgents = AppConfig.getRemoteAgents();
        
        registerWorkerTypes();
    }
    
    private void registerWorkerTypes() {
        workerConfigs.put("nota", new WorkerInfo("nota", "sdProject.network.workers.NotaWorker", AppConfig.getNotaWorkerPort()));
        workerConfigs.put("matricula", new WorkerInfo("matricula", "sdProject.network.workers.MatriculaWorker", AppConfig.getMatriculaWorkerPort()));
        workerConfigs.put("historico", new WorkerInfo("historico", "sdProject.network.workers.HistoricoWorker", AppConfig.getHistoricoWorkerPort()));
    }
    
    @SuppressWarnings("unused")
    public void start() {
        final String GREEN = "\u001B[32m";
        final String YELLOW = "\u001B[33m";
        final String RED = "\u001B[31m";
        final String RESET = "\u001B[0m";
        scheduler.scheduleAtFixedRate(this::checkWorkersHealth, 
            AppConfig.getHealthCheckIntervalSeconds(), 
            AppConfig.getHealthCheckIntervalSeconds() * 3, 
            TimeUnit.SECONDS);
        System.out.println(GREEN + "WorkerMonitor iniciado. Monitorando workers..." + RESET);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getAvailableServicesUDP(String serviceType){
        Map<String, String> result = new HashMap<>();
        Map<String, Object> request = new HashMap<>();

        request.put("operation", "getServices");
        request.put("serviceType", serviceType);

        try (Connection connection = new Connection()){
            connection.setSoTimeout(UDP_TIMEOUT_MS);
            InetAddress gwAddress = InetAddress.getByName(gatewayHost);

            connection.sendUDP(request, gwAddress, gatewayPort);

            DatagramPacket receivePacket = connection.receiveUDP();
            byte[] actualData = new byte[receivePacket.getLength()];
            System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), actualData, 0, receivePacket.getLength());

            Map<String, Object> response = (Map<String, Object>) SerializationUtils.deserialize(actualData);

            if ("success".equals(response.get("status"))){
                Map<String, String> services = (Map<String, String>) response.get("services");
                if (services != null){
                    return services;
                }
            } else {
                System.err.println("Gateway falhou ao obter serviços para " + serviceType + ": " + response.get("message"));
            }
        } catch (SocketTimeoutException e){
            System.err.println("Timeout ao obter serviços do tipo " + serviceType);
        } catch (IOException | ClassNotFoundException e){
            System.err.println("Erro ao obter serviços do tipo " + serviceType + " " + e.getMessage());
        }

        return result;
    }
    
    private void checkWorkersHealth() {
        final String GREEN = "\u001B[32m";
        final String YELLOW = "\u001B[33m";
        final String RED = "\u001B[31m";
        final String RESET = "\u001B[0m";
        System.out.println(YELLOW + "Verificando saúde dos workers..." + RESET);
        try {
            for (String serviceType : workerConfigs.keySet()) {
                Map<String, String> availableServices = getAvailableServicesUDP(serviceType);
                if (availableServices.isEmpty()) {
                    System.out.println(RED + "Nenhum serviço do tipo " + serviceType + " encontrado. Solicitando novo worker aos agents remotos..." + RESET);
                    requestWorkerFromRemoteAgent(serviceType);
                } else {
                    System.out.println(GREEN + "Encontrados " + availableServices.size() + " serviços do tipo " + serviceType + RESET);
                }
            }
        } catch (Exception e) {
            System.err.println(RED + "Erro ao verificar saúde dos workers: " + e.getMessage() + RESET);
        }
    }
    
    private void requestWorkerFromRemoteAgent(String workerType) {
        for (String agentAddress : remoteAgents) {
            try {
                String[] parts = agentAddress.split(":");
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                
                Map<String, Object> request = new HashMap<>();
                request.put("operation", "startWorker");
                request.put("workerType", workerType);
                
                try (Socket socket = new Socket(host, port);
                     Connection connection = new Connection(socket)) {
                    
                    connection.send(request);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) connection.receive();
                    
                    if ("success".equals(response.get("status"))) {
                        System.out.println("Worker " + workerType + " iniciado com sucesso no agent " + agentAddress);
                        return; // Sucesso, não precisa tentar outros agents
                    } else {
                        System.err.println("Agent " + agentAddress + " falhou ao iniciar worker " + workerType + ": " + response.get("message"));
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Erro ao comunicar com agent " + agentAddress + ": " + e.getMessage());
            }
        }
        
        System.err.println("Falha ao iniciar worker " + workerType + " em todos os agents remotos disponíveis");
    }
    
    public void stop() {
        final String YELLOW = "\u001B[33m";
        final String RESET = "\u001B[0m";
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)){
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e){
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println(YELLOW + "WorkerMonitor parado" + RESET);
    }
    
    public static void main(String[] args) {
        String gatewayHost = AppConfig.getGatewayHost();
        int gatewayPort = AppConfig.getGatewayPort();
        
        if (args.length >= 2) {
            gatewayHost = args[0];
            gatewayPort = Integer.parseInt(args[1]);
        }
        
        WorkerMonitor monitor = new WorkerMonitor(gatewayHost, gatewayPort);
        monitor.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Parando WorkerMonitor...");
            monitor.stop();
        }));
    }
}
