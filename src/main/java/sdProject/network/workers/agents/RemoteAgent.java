package sdProject.network.workers.agents;

import sdProject.config.AppConfig;
import sdProject.network.util.Connection;
import sdProject.network.util.SerializationUtils;
import sdProject.network.workers.monitor.WorkerInfo;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;

public class RemoteAgent {
    private final int port;
    private final String agentId;
    private final ExecutorService threadPool;
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private final Map<String, WorkerInfo> workerConfigs;
    private final Map<String, Process> activeProcesses;
    private final String gatewayHost;
    private final int gatewayPort;

    public RemoteAgent(int port, String agentId, String gatewayHost, int gatewayPort) {
        this.port = port;
        this.agentId = agentId;
        this.gatewayHost = gatewayHost;
        this.gatewayPort = gatewayPort;
        this.threadPool = Executors.newFixedThreadPool(5);
        this.workerConfigs = new HashMap<>();
        this.activeProcesses = new HashMap<>();
        
        registerWorkerTypes();
    }

    private void registerWorkerTypes() {
        workerConfigs.put("nota", new WorkerInfo("nota", "sdProject.network.workers.NotaWorker", AppConfig.getNotaWorkerPort()));
        workerConfigs.put("matricula", new WorkerInfo("matricula", "sdProject.network.workers.MatriculaWorker", AppConfig.getMatriculaWorkerPort()));
        workerConfigs.put("historico", new WorkerInfo("historico", "sdProject.network.workers.HistoricoWorker", AppConfig.getHistoricoWorkerPort()));
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("RemoteAgent " + agentId + " iniciado na porta " + port);
            
            new Thread(this::acceptConnections).start();
            
        } catch (IOException e) {
            System.err.println("Erro ao iniciar RemoteAgent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleRequest(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleRequest(Socket clientSocket) {
        try (Connection connection = new Connection(clientSocket)) {
            Map<String, Object> request = (Map<String, Object>) connection.receive();
            Map<String, Object> response = processRequest(request);
            connection.send(response);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao processar requisição: " + e.getMessage());
        }
    }

    private Map<String, Object> processRequest(Map<String, Object> request) {
        String operation = (String) request.get("operation");
        
        switch (operation) {
            case "startWorker":
                return startWorker(request);
            case "stopWorker":
                return stopWorker(request);
            case "status":
                return getStatus();
            default:
                return createErrorResponse("Operação desconhecida: " + operation);
        }
    }

    private Map<String, Object> startWorker(Map<String, Object> request) {
        String workerType = (String) request.get("workerType");
        
        if (workerType == null) {
            return createErrorResponse("workerType é obrigatório");
        }
        
        WorkerInfo info = workerConfigs.get(workerType);
        if (info == null) {
            return createErrorResponse("Tipo de worker desconhecido: " + workerType);
        }
        
        try {
            int port = findAvailablePort(info.getInitialPort());
            String processKey = workerType + "-" + port;
            
            if (activeProcesses.containsKey(processKey) && activeProcesses.get(processKey).isAlive()) {
                return createErrorResponse("Worker " + workerType + " já está rodando na porta " + port);
            }
            
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add(info.getClassName());
            command.add(String.valueOf(port));
            command.add(gatewayHost);
            command.add(String.valueOf(gatewayPort));
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();
            
            Process process = pb.start();
            activeProcesses.put(processKey, process);
            
            new Thread(() -> {
                try {
                    int exitCode = process.waitFor();
                    System.out.println("Worker " + workerType + " na porta " + port + " terminou com código " + exitCode);
                    activeProcesses.remove(processKey);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    activeProcesses.remove(processKey);
                }
            }).start();
            
            System.out.println("Agent " + agentId + " iniciou worker " + workerType + " na porta " + port);
            
            Map<String, Object> response = createSuccessResponse("Worker " + workerType + " iniciado na porta " + port);
            response.put("port", port);
            response.put("agentId", agentId);
            return response;
            
        } catch (IOException e) {
            return createErrorResponse("Erro ao iniciar worker: " + e.getMessage());
        }
    }

    private Map<String, Object> stopWorker(Map<String, Object> request) {
        String workerType = (String) request.get("workerType");
        Integer port = (Integer) request.get("port");
        
        if (workerType == null) {
            return createErrorResponse("workerType é obrigatório");
        }
        
        String processKey = workerType + "-" + (port != null ? port : "");
        
        if (port != null) {
            processKey = workerType + "-" + port;
            Process process = activeProcesses.get(processKey);
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                activeProcesses.remove(processKey);
                return createSuccessResponse("Worker " + workerType + " parado");
            }
        } else {
            // Para todos os workers deste tipo
            boolean found = false;
            for (String key : new ArrayList<>(activeProcesses.keySet())) {
                if (key.startsWith(workerType + "-")) {
                    Process process = activeProcesses.get(key);
                    if (process != null && process.isAlive()) {
                        process.destroyForcibly();
                        activeProcesses.remove(key);
                        found = true;
                    }
                }
            }
            if (found) {
                return createSuccessResponse("Workers " + workerType + " parados");
            }
        }
        
        return createErrorResponse("Worker não encontrado");
    }

    private Map<String, Object> getStatus() {
        Map<String, Object> response = createSuccessResponse("Status do agent " + agentId);
        Map<String, Object> workers = new HashMap<>();
        
        for (Map.Entry<String, Process> entry : activeProcesses.entrySet()) {
            workers.put(entry.getKey(), entry.getValue().isAlive() ? "running" : "stopped");
        }
        
        response.put("activeWorkers", workers);
        response.put("agentId", agentId);
        return response;
    }

    private int findAvailablePort(int startingPort) {
        int port = startingPort;
        
        while (true) {
            if (isPortAvailable(port)) {
                return port;
            }
            port++;
        }
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }

    public void stop() {
        running = false;
        threadPool.shutdown();
        
        synchronized (activeProcesses) {
            for (Process process : activeProcesses.values()) {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }
            activeProcesses.clear();
        }
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao parar RemoteAgent: " + e.getMessage());
        }
        
        System.out.println("RemoteAgent " + agentId + " parado");
    }

    public static void main(String[] args) {
        // Usar configurações padrão do AppConfig
        String agentId = "agent-" + System.currentTimeMillis(); // ID único baseado em timestamp
        int port = AppConfig.getRemoteAgentPort();
        String gatewayHost = AppConfig.getGatewayHost();
        int gatewayPort = AppConfig.getGatewayPort();
        
        // Permitir sobrescrever com argumentos opcionais
        if (args.length > 0) {
            agentId = args[0];
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            gatewayHost = args[2];
        }
        if (args.length > 3) {
            gatewayPort = Integer.parseInt(args[3]);
        }
        
        // Tornar final para uso no shutdown hook
        final String finalAgentId = agentId;
        
        RemoteAgent agent = new RemoteAgent(port, finalAgentId, gatewayHost, gatewayPort);
        agent.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Parando RemoteAgent " + finalAgentId + "...");
            agent.stop();
        }));
    }
}
