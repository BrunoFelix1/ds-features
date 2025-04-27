package sdProject.network.workers;

import sdProject.controllers.NotaController;
import java.sql.SQLException;
import java.util.Map;

public class NotaWorker extends BaseWorker {
    
    private final NotaController notaController;

    public NotaWorker(int port, String gatewayHost, int gatewayPort) throws SQLException {
        super(port, "NotaWorker", "nota", 10, gatewayHost, gatewayPort);
        this.notaController = new NotaController();
    }
    

    public NotaWorker(int port) throws SQLException {
        super(port, "NotaWorker", "nota", 10);
        this.notaController = new NotaController();
    }
    

    @Override
    protected Map<String, Object> processRequest(Map<String, Object> request) {
        String action = (String) request.get("action");
        
        if (action == null) {
            return createErrorResponse("Ação não especificada");
        }
        
        try {
            switch (action) {
                case "registrarNota":
                    return registrarNota(request);
                case "consultarNota":
                    return consultarNota(request);
                case "calcularMediaAluno":
                    return calcularMediaAluno(request);
                case "calcularMediaDisciplina":
                    return calcularMediaDisciplina(request);
                default:
                    return createErrorResponse("Ação desconhecida: " + action);
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar requisição: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("Erro interno: " + e.getMessage());
        }
    }
    

    private Map<String, Object> registrarNota(Map<String, Object> request) {
        Integer alunoId = (Integer) request.get("alunoId");
        Integer disciplinaId = (Integer) request.get("disciplinaId");
        Double nota = (Double) request.get("nota");
        
        if (alunoId == null || disciplinaId == null) {
            return createErrorResponse("alunoId e disciplinaId são obrigatórios");
        }
        
        return notaController.registrarNota(alunoId, disciplinaId, nota);
    }
    

    private Map<String, Object> consultarNota(Map<String, Object> request) {
        Integer alunoId = (Integer) request.get("alunoId");
        Integer disciplinaId = (Integer) request.get("disciplinaId");
        
        if (alunoId == null || disciplinaId == null) {
            return createErrorResponse("alunoId e disciplinaId são obrigatórios");
        }
        
        return notaController.consultarNota(alunoId, disciplinaId);
    }
    

    private Map<String, Object> calcularMediaAluno(Map<String, Object> request) {
        Integer alunoId = (Integer) request.get("alunoId");
        
        if (alunoId == null) {
            return createErrorResponse("alunoId é obrigatório");
        }
        
        return notaController.calcularMediaAluno(alunoId);
    }
    

    private Map<String, Object> calcularMediaDisciplina(Map<String, Object> request) {
        Integer disciplinaId = (Integer) request.get("disciplinaId");
        
        if (disciplinaId == null) {
            return createErrorResponse("disciplinaId é obrigatório");
        }
        
        return notaController.calcularMediaDisciplina(disciplinaId);
    }

    public static void main(String[] args) {
        try {
            int port = 8082; // Porta padrão
            String gatewayHost = "localhost";
            int gatewayPort = 8080;
            
            // Processar argumentos da linha de comando
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
            
            if (args.length > 2) {
                gatewayHost = args[1];
                gatewayPort = Integer.parseInt(args[2]);
            }
            
            NotaWorker worker = new NotaWorker(port, gatewayHost, gatewayPort);
            worker.start();
            
            // Adiciona shutdown hook para parar o worker quando o JVM for encerrado
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Parando NotaWorker...");
                worker.stop();
            }));
            
            System.out.println("Registrado no Gateway Discovery em " + gatewayHost + ":" + gatewayPort);
            
        } catch (Exception e) {
            System.err.println("Erro ao iniciar NotaWorker: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
