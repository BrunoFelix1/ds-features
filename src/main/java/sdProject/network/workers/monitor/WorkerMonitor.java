package sdProject.network.workers.monitor;

import sdProject.network.util.SerializationUtils;

import java.io.IOException;
import java.net.*;
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

    private static final int UDP_TIMEOUT_MS = 5000;
    private static final int MAX_PACKET_SIZE = 65507;
    

    
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

    @SuppressWarnings("unchecked")
    private Map<String, String> getAvailableServicesUDP(String serviceType){
        Map<String, String> result = new HashMap<>();
        Map<String, Object> request = new HashMap<>();

        request.put("operation", "getServices");
        request.put("serviceType", serviceType);

        try (DatagramSocket udpSocket = new DatagramSocket()){
            udpSocket.setSoTimeout(UDP_TIMEOUT_MS);
            InetAddress gwAddress = InetAddress.getByName(gatewayHost);

            byte[] sendData = SerializationUtils.serialize(request);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, gwAddress, gatewayPort);
            udpSocket.send(sendPacket);

            byte[] receiveBuffer = new byte[MAX_PACKET_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            udpSocket.receive(receivePacket);

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

        return result; // Em caso de erro ou timeout, retorna vazio.
    }
    
    private void checkWorkersHealth() {
        System.out.println("Verificando saúde dos workers...");
        
        try {
            for (String serviceType : workerConfigs.keySet()) {
                Map<String, String> availableServices = getAvailableServicesUDP(serviceType);
                
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
            if (activeProcesses.containsKey(processKey) && activeProcesses.get(processKey).isAlive()) {
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
                    System.err.println("Thread de monitoramento do worker " + serviceType + " interrompida.");
                    activeProcesses.remove(processKey);
                }
            }).start();
            
        } catch (IOException e) {
            System.err.println("Erro ao iniciar novo worker " + serviceType + ": " + e.getMessage());
        }
    }
    
    private int findAvailablePort(int startingPort) {
        int port = startingPort;
        
        // Verifica se a porta já está sendo usada por algum processo ativo
        synchronized (activeProcesses){
            boolean portInUseByActiveProcess;
            do{
                portInUseByActiveProcess = false;
                for (String processKey : activeProcesses.keySet()) {
                    if (processKey.endsWith("-" + port) && activeProcesses.get(processKey).isAlive()) {
                        // Porta já em uso, incrementa
                        port++;
                        portInUseByActiveProcess = true;
                        break;
                    }
                }
            } while (portInUseByActiveProcess);
        }


        // Procura por uma porta disponível e retorna a primeira que encontrar.
        while (true){
            if (isPortAvailable(port)){
                return port;
            } else {
                port++;
            }
        }
    }
    
    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            // Se conseguir abrir um socket, a porta está disponível
            return true;
        } catch (IOException e) {
            // Se der exceção, a porta está em uso
            return false;
        }
    }
    
    public void stop() {
        scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)){
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e){
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        synchronized (activeProcesses){
            // Para todos os processos ativos
            for (Process process : activeProcesses.values()) {
                if (process.isAlive()){
                    process.destroyForcibly();
                }
            }
            activeProcesses.clear();
        }

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
