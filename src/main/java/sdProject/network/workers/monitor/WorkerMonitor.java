package sdProject.network.workers.monitor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;


public class WorkerMonitor {
    private final String gatewayHost;
    private final int gatewayPort;
    private final ScheduledExecutorService scheduler;
    private final Map<String, WorkerInfo> workerConfigs;
    private final Map<String, Process> activeProcesses;
    

    
    public WorkerMonitor(String gatewayHost, int gatewayPort) {
        this.gatewayHost = gatewayHost;
        this.gatewayPort = gatewayPort;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.workerConfigs = new HashMap<>();
        this.activeProcesses = new HashMap<>();
        
        registerWorkerTypes();
    }
    
    private void registerWorkerTypes() {
        workerConfigs.put("nota", new WorkerInfo("nota", "sdProject.network.workers.NotaWorker", 8082));
        workerConfigs.put("matricula", new WorkerInfo("matricula", "sdProject.network.workers.MatriculaWorker", 8081));
        workerConfigs.put("historico", new WorkerInfo("historico", "sdProject.network.workers.HistoricoWorker", 8083));
    }
    
    public void start() {
        scheduler.scheduleAtFixedRate(this::checkWorkersHealth, 10, 30, TimeUnit.SECONDS);
        
        System.out.println("WorkerMonitor iniciado. Monitorando workers...");
    }
    
    private void checkWorkersHealth() {
        System.out.println("Verificando saúde dos workers...");
        
        try {
            for (String serviceType : workerConfigs.keySet()) {
                Map<String, String> availableServices = getAvailableServices(serviceType);
                
                if (availableServices.isEmpty()) {
                    boolean hasActiveWorker = false;
                    for (String key : activeProcesses.keySet()) {
                        if (key.startsWith(serviceType + "-")) {
                            Process process = activeProcesses.get(key);
                            if (process.isAlive()) {
                                hasActiveWorker = true;
                                System.out.println("Worker " + serviceType + " está rodando, mas ainda não registrado no gateway.");
                                break;
                            }
                        }
                    }
                    
                    if (!hasActiveWorker) {
                        System.out.println("Nenhum serviço do tipo " + serviceType + " encontrado. Iniciando um novo...");
                        startNewWorker(serviceType);
                    }
                } else {
                    System.out.println("Encontrados " + availableServices.size() + " serviços do tipo " + serviceType);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar saúde dos workers: " + e.getMessage());
        }
    }
    
    private Map<String, String> getAvailableServices(String serviceType) {
        Map<String, String> result = new HashMap<>();
        
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("operation", "getServices");
            request.put("serviceType", serviceType);
            
            try (Socket socket = new Socket(gatewayHost, gatewayPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                out.writeObject(request);
                
                Map<String, Object> response = (Map<String, Object>) in.readObject();
                
                if ("success".equals(response.get("status"))) {
                    Map<String, String> services = (Map<String, String>) response.get("services");
                    if (services != null) {
                        return services;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter serviços do tipo " + serviceType + ": " + e.getMessage());
        }
        
        return result;
    }
    
    private void startNewWorker(String serviceType) {
        WorkerInfo info = workerConfigs.get(serviceType);
        if (info == null) {
            System.err.println("Configuração não encontrada para o tipo de serviço: " + serviceType);
            return;
        }
        
        try {
            // Encontra uma porta disponível
            int port = findAvailablePort(info.initialPort);
            
            // Verifica se já existe um worker deste tipo rodando
            String processKey = serviceType + "-" + port;
            if (activeProcesses.containsKey(processKey)) {
                System.out.println("Worker " + serviceType + " já está rodando na porta " + port);
                return;
            }
            
            // Constrói o comando para iniciar o worker
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add(info.className);
            command.add(String.valueOf(port));
            command.add(gatewayHost);
            command.add(String.valueOf(gatewayPort));
            
            System.out.println("Iniciando worker " + serviceType + " na porta " + port);
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO(); // Redireciona saída do processo para o console
            
            Process process = pb.start();
            activeProcesses.put(processKey, process);
            
            // Monitor para o processo
            new Thread(() -> {
                try {
                    int exitCode = process.waitFor();
                    System.out.println("Worker " + serviceType + " na porta " + port + " terminou com código " + exitCode);
                    activeProcesses.remove(processKey);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
        } catch (IOException e) {
            System.err.println("Erro ao iniciar novo worker " + serviceType + ": " + e.getMessage());
        }
    }
    
    private int findAvailablePort(int startingPort) {
        int port = startingPort;
        
        // Verifica se a porta já está sendo usada por algum processo ativo
        for (String processKey : activeProcesses.keySet()) {
            if (processKey.endsWith("-" + port)) {
                // Porta já em uso, incrementa
                port++;
            }
        }
        
        // Verifica se a porta está realmente disponível tentando abrir um socket
        while (!isPortAvailable(port)) {
            port++;
        }
        
        return port;
    }
    
    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            // Se conseguir abrir um socket, a porta está disponível
            return true;
        } catch (IOException e) {
            // Se der exceção, a porta está em uso
            return false;
        }
    }
    
    public void stop() {
        scheduler.shutdown();
        
        // Para todos os processos ativos
        for (Process process : activeProcesses.values()) {
            try {
                process.destroy();
            } catch (Exception e) {
                System.err.println("Erro ao parar processo: " + e.getMessage());
            }
        }
        
        activeProcesses.clear();
        System.out.println("WorkerMonitor parado");
    }
    
    public static void main(String[] args) {
        String gatewayHost = "localhost";
        int gatewayPort = 8080;
        
        // Verificar se host e porta foram passados como argumentos
        if (args.length >= 2) {
            gatewayHost = args[0];
            gatewayPort = Integer.parseInt(args[1]);
        }
        
        WorkerMonitor monitor = new WorkerMonitor(gatewayHost, gatewayPort);
        monitor.start();
        
        // Adiciona shutdown hook para parar o monitor quando o JVM for encerrado
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Parando WorkerMonitor...");
            monitor.stop();
        }));
    }
}
