package sdProject.network.workers;

import sdProject.config.AppConfig;
import sdProject.controllers.MatriculaController;
import java.sql.SQLException;
import java.util.Map;

public class MatriculaWorker extends BaseWorker {
    
    private final MatriculaController matriculaController;
    

    public MatriculaWorker(int port, String gatewayHost, int gatewayPort) throws SQLException {
        super(port, "MatriculaWorker", "matricula", 10, gatewayHost, gatewayPort);
        this.matriculaController = new MatriculaController();
    }
    

    public MatriculaWorker(int port) throws SQLException {
        super(port, "MatriculaWorker", "matricula", 10);
        this.matriculaController = new MatriculaController();
    }
    

    @Override
    protected Map<String, Object> processRequest(Map<String, Object> request) {
        String action = (String) request.get("action");
        
        if (action == null) {
            return createErrorResponse("Ação não especificada");
        }
        
        try {
            switch (action) {
                case "matricular":
                    return matricularAluno(request);
                case "cancelar":
                    return cancelarMatricula(request);
                case "buscarPorAluno":
                    return buscarMatriculasPorAluno(request);
                case "buscarPorDisciplina":
                    return buscarMatriculasPorDisciplina(request);
                case "verificarMatricula":
                    return verificarMatricula(request);
                default:
                    return createErrorResponse("Ação desconhecida: " + action);
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar requisição: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("Erro interno: " + e.getMessage());
        }
    }

    private Map<String, Object> matricularAluno(Map<String, Object> request) {
        Integer alunoId = (Integer) request.get("alunoId");
        Integer disciplinaId = (Integer) request.get("disciplinaId");
        
        if (alunoId == null || disciplinaId == null) {
            return createErrorResponse("alunoId e disciplinaId são obrigatórios");
        }
        
        return matriculaController.matricularAluno(alunoId, disciplinaId);
    }

    private Map<String, Object> cancelarMatricula(Map<String, Object> request) {
        Integer alunoId = (Integer) request.get("alunoId");
        Integer disciplinaId = (Integer) request.get("disciplinaId");
        
        if (alunoId == null || disciplinaId == null) {
            return createErrorResponse("alunoId e disciplinaId são obrigatórios");
        }
        
        return matriculaController.cancelarMatricula(alunoId, disciplinaId);
    }
    

    private Map<String, Object> buscarMatriculasPorAluno(Map<String, Object> request) {
        Integer alunoId = (Integer) request.get("alunoId");
        
        if (alunoId == null) {
            return createErrorResponse("alunoId é obrigatório");
        }
        
        return matriculaController.listarMatriculasPorAluno(alunoId);
    }
    

    private Map<String, Object> buscarMatriculasPorDisciplina(Map<String, Object> request) {
        Integer disciplinaId = (Integer) request.get("disciplinaId");
        
        if (disciplinaId == null) {
            return createErrorResponse("disciplinaId é obrigatório");
        }
        
        return matriculaController.listarMatriculasPorDisciplina(disciplinaId);
    }

    private Map<String, Object> verificarMatricula(Map<String, Object> request) {
        Integer alunoId = (Integer) request.get("alunoId");
        Integer disciplinaId = (Integer) request.get("disciplinaId");
        
        if (alunoId == null || disciplinaId == null) {
            return createErrorResponse("alunoId e disciplinaId são obrigatórios");
        }
        
        return matriculaController.verificarMatricula(alunoId, disciplinaId);
    }

    public static void main(String[] args) {
        try {
            int port = AppConfig.getMatriculaWorkerPort(); // Porta padrão da configuração
            String gatewayHost = AppConfig.getGatewayHost();
            int gatewayPort = AppConfig.getGatewayPort();
            
            // Processar argumentos da linha de comando (sobrescreve as configurações)
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
            
            if (args.length > 2) {
                gatewayHost = args[1];
                gatewayPort = Integer.parseInt(args[2]);
            }
            
            MatriculaWorker worker = new MatriculaWorker(port, gatewayHost, gatewayPort);
            worker.start();
            
            // Adiciona shutdown hook para parar o worker quando o JVM for encerrado
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Parando MatriculaWorker...");
                worker.stop();
            }));
            
            System.out.println("Registrado no Gateway Discovery em " + gatewayHost + ":" + gatewayPort);
            
        } catch (Exception e) {
            System.err.println("Erro ao iniciar MatriculaWorker: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
