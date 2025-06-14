package sdProject.network.workers;

import sdProject.config.AppConfig;
import sdProject.controllers.HistoricoController;
import java.sql.SQLException;
import java.util.Map;

public class HistoricoWorker extends BaseWorker {
    
    private final HistoricoController historicoController;
    

    public HistoricoWorker(int port, String gatewayHost, int gatewayPort) throws SQLException {
        super(port, "HistoricoWorker", "historico", 10, gatewayHost, gatewayPort);
        this.historicoController = new HistoricoController();
    }
    

    public HistoricoWorker(int port) throws SQLException {
        super(port, "HistoricoWorker", "historico", 10);
        this.historicoController = new HistoricoController();
    }
    

    @Override
    protected Map<String, Object> processRequest(Map<String, Object> request) {
        String action = (String) request.get("action");
        
        if (action == null) {
            return createErrorResponse("Ação não especificada");
        }
        
        try {
            switch (action) {
                case "historicoCompleto":
                    return gerarHistoricoCompleto(request);
                case "disciplinasAprovadas":
                    return listarDisciplinasAprovadas(request);
                case "disciplinasReprovadas":
                    return listarDisciplinasReprovadas(request);
                case "disciplinasEmCurso":
                    return listarDisciplinasEmCurso(request);
                default:
                    return createErrorResponse("Ação desconhecida: " + action);
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar requisição: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("Erro interno: " + e.getMessage());
        }
    }
    

    private Map<String, Object> gerarHistoricoCompleto(Map<String, Object> request) {
        Integer alunoId = (Integer) request.get("alunoId");
        
        if (alunoId == null) {
            return createErrorResponse("alunoId é obrigatório");
        }
        
        return historicoController.gerarHistoricoCompleto(alunoId);
    }
    

    private Map<String, Object> listarDisciplinasAprovadas(Map<String, Object> request) {
        Integer alunoId = (Integer) request.get("alunoId");
        
        if (alunoId == null) {
            return createErrorResponse("alunoId é obrigatório");
        }
        
        return historicoController.listarDisciplinasAprovadas(alunoId);
    }
    

    private Map<String, Object> listarDisciplinasReprovadas(Map<String, Object> request) {
        Integer alunoId = (Integer) request.get("alunoId");
        
        if (alunoId == null) {
            return createErrorResponse("alunoId é obrigatório");
        }
        
        return historicoController.listarDisciplinasReprovadas(alunoId);
    }
    

    private Map<String, Object> listarDisciplinasEmCurso(Map<String, Object> request) {
        Integer alunoId = (Integer) request.get("alunoId");
        
        if (alunoId == null) {
            return createErrorResponse("alunoId é obrigatório");
        }
        
        return historicoController.listarDisciplinasEmCurso(alunoId); 
    }
       
    
    public static void main(String[] args) {
        try {
            int port = AppConfig.getHistoricoWorkerPort(); // Porta padrão da configuração
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
            
            HistoricoWorker worker = new HistoricoWorker(port, gatewayHost, gatewayPort);
            worker.start();
            
            // Adiciona shutdown hook para parar o worker quando o JVM for encerrado
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Parando HistoricoWorker...");
                worker.stop();
            }));
            
            System.out.println("Registrado no Gateway Discovery em " + gatewayHost + ":" + gatewayPort);
            
        } catch (Exception e) {
            System.err.println("Erro ao iniciar HistoricoWorker: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
