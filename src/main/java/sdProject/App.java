package sdProject;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import sdProject.config.DatabaseConnection;
import sdProject.models.Disciplina;
import sdProject.models.Matricula;
import sdProject.network.client.TestMainClient;

public class App {
    private static TestMainClient cliente; //Cliente distribuído como variável global
    
    public static void main(String[] args) {

        //Instancia o cliente, com o host e a porta do gateway com quem ele vai se comunicar
        String gatewayHost = "localhost";
        int gatewayPort = 8080;
        
        // Verificar se host e porta foram passados como argumentos
        if (args.length >= 2) {
            gatewayHost = args[0];
            gatewayPort = Integer.parseInt(args[1]);
        }
        cliente = new TestMainClient(gatewayHost, gatewayPort); //Inicializa cliente global

        //Migrations 
        System.out.println("Iniciando aplicação");
        
        try {
            System.out.println("Executando migrações do banco de dados...");
            DatabaseConnection.runMigrations();
            System.out.println("Migrações concluídas com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro ao executar migrações: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Aplicação finalizada");
        }

        //Menu do nosso sistema
        Scanner scanner = new Scanner(System.in);
        Integer escolhaDoServico = -1;

        do{
            System.out.println("============================================================");
            System.out.println("SEJA BEM VINDO AO SISTEMA DE GERÊNCIAMENTO ACADÊMICO!");
            System.out.println("Qual serviço você quer acessar?(Digite um número de 1-4)");
            System.out.println("1.Matrícula \n2.Nota \n3.Histórico \n4.Sair");
            System.out.println("============================================================");

            escolhaDoServico = scanner.nextInt();
            
            switch (escolhaDoServico) {
                case 1:
                    menuMatricula(escolhaDoServico, scanner);
                    break;
                case 2:
                    menuNota(escolhaDoServico, scanner);
                    break; 
                case 3:
                    menuHistorico(escolhaDoServico, scanner);
                    break;
                case 4:
                    scanner.close();
                    System.out.println("Encerrando sistema...");
                    System.exit(0);
                    break;
            
                default:
                    System.out.println("Digite um número válido!");
                    break;
            }

        } while(escolhaDoServico != 0);

        scanner.close();
    }

    // MENU MATRÍCULA 
    public static void menuMatricula(Integer escolhaDoSubservico, Scanner scanner) {
        escolhaDoSubservico = -1;

        do{
            System.out.println("Qual subserviço você quer acessar?");
            System.out.println("1.Matricular aluno \n" +
            "2.Verificar matrícula \n" +
            "3.Listar matrículas por aluno \n" +
            "4.Listar matrículas por disciplina \n" +
            "0.Voltar");

            escolhaDoSubservico = scanner.nextInt();

            if (escolhaDoSubservico == 0) {
                System.out.println("Voltando...");
                break;
            }

            System.out.println("Digite o ID do aluno: ");
            int alunoId = scanner.nextInt();
            System.out.println("Digite o ID da disciplina: ");
            int disciplinaId = scanner.nextInt();
    
            switch (escolhaDoSubservico) {
                case 1:
                    System.out.println("Matriculando aluno " + alunoId + " na disciplina " + disciplinaId);
                    
                    //CHAMA O CLIENTE DISTRIBUÍDO
                    Map<String, Object> resultadoMatricula = cliente.matricularAluno(alunoId, disciplinaId);
                    
                    if ("success".equals(resultadoMatricula.get("status"))) {
                        System.out.println("Matrícula realizada com sucesso!");
                        System.out.println("Mensagem: " + resultadoMatricula.get("message"));
                    } else {
                        System.out.println("Erro na matrícula: " + resultadoMatricula.get("message"));
                    }
                    break;
                    
                case 2:
                    System.out.println("Verificando matrícula do aluno " + alunoId + " na disciplina " + disciplinaId);
                    
                    //Criar nova operação para verificar matrícula
                    Map<String, Object> resultadoVerificar = chamarServicoMatricula("verificar", alunoId, disciplinaId, null);
                    
                    if ("success".equals(resultadoVerificar.get("status"))) {
                        boolean matriculado = (Boolean) resultadoVerificar.get("matriculado");
                        System.out.println("Aluno matriculado? " + matriculado);
                    } else {
                        System.out.println("Erro ao verificar: " + resultadoVerificar.get("message"));
                    }
                    break;
                    
                case 3:
                    System.out.println("Listando matrículas do aluno " + alunoId);
                    
                    //Criar nova operação para listar por aluno
                    Map<String, Object> resultadoListarAluno = chamarServicoMatricula("listarPorAluno", alunoId, null, null);
                    
                    if ("success".equals(resultadoListarAluno.get("status"))) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> matriculas = (List<Map<String, Object>>) resultadoListarAluno.get("matriculas");
                        
                        if (matriculas.isEmpty()) {
                            System.out.println("Nenhuma matrícula encontrada para o aluno ID " + alunoId);
                        } else {
                            for (Map<String, Object> matricula : matriculas) {
                                System.out.println("Disciplina ID: " + matricula.get("disciplinaId") + " | Nota: " + matricula.get("nota"));
                            }
                        }
                    } else {
                        System.out.println("Erro ao listar: " + resultadoListarAluno.get("message"));
                    }
                    break;
                    
                case 4:
                    System.out.println("Listando matrículas da disciplina " + disciplinaId);
                    
                    //Criar nova operação para listar por disciplina
                    Map<String, Object> resultadoListarDisciplina = chamarServicoMatricula("listarPorDisciplina", null, disciplinaId, null);
                    
                    if ("success".equals(resultadoListarDisciplina.get("status"))) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> matriculas = (List<Map<String, Object>>) resultadoListarDisciplina.get("matriculas");
                        
                        if (matriculas.isEmpty()) {
                            System.out.println("Nenhuma matrícula encontrada para a disciplina ID " + disciplinaId);
                        } else {
                            System.out.println("Matrículas da disciplina ID " + disciplinaId + ":");
                            for (Map<String, Object> matricula : matriculas) {
                                System.out.println("Aluno ID: " + matricula.get("alunoId") + " | Nota: " + matricula.get("nota"));
                            }
                        }
                    } else {
                        System.out.println("Erro ao listar: " + resultadoListarDisciplina.get("message"));
                    }
                    break;
                    
                default:
                    System.out.println("Opção inválida, tente novamente.");
            }
        } while (escolhaDoSubservico != 0);
    }

    //MENU NOTA - Usando cliente distribuído
    public static void menuNota(Integer escolhaDoSubservico, Scanner scanner) {
        escolhaDoSubservico = -1;

        do{
            System.out.println("Qual subserviço você quer acessar?");
            System.out.println("1.Registrar nota \n" +
            "2.Consultar nota \n" +
            "3.Calcular média aluno \n" +
            "4.Calcular média disciplina \n" +
            "0.Voltar");

            escolhaDoSubservico = scanner.nextInt();

            if (escolhaDoSubservico == 0) {
                System.out.println("Voltando...");
                break;
            }

            System.out.println("Digite o ID do aluno: ");
            int alunoId = scanner.nextInt();
            System.out.println("Digite o ID da disciplina: ");
            int disciplinaId = scanner.nextInt(); 

            switch (escolhaDoSubservico) {
                case 1:
                    System.out.println("Digite a nota: ");
                    double nota = scanner.nextDouble();
                    
                    //CHAMA O CLIENTE DISTRIBUÍDO
                    Map<String, Object> resultadoNota = cliente.registrarNota(alunoId, disciplinaId, nota);
                    
                    if ("success".equals(resultadoNota.get("status"))) {
                        System.out.println("Nota registrada com sucesso!");
                        System.out.println("Mensagem: " + resultadoNota.get("message"));
                    } else {
                        System.out.println("Erro ao registrar nota: " + resultadoNota.get("message"));
                    }
                    break;
                    
                case 2:
                    System.out.println("Consultando nota...");
                    
                    Map<String, Object> resultadoConsulta = chamarServicoNota("consultar", alunoId, disciplinaId, null);
                    
                    if ("success".equals(resultadoConsulta.get("status"))) {
                        Double notaConsultada = (Double) resultadoConsulta.get("nota");
                        if (notaConsultada != null) {
                            System.out.println("Nota do aluno " + alunoId + " na disciplina " + disciplinaId + ": " + notaConsultada);
                        } else {
                            System.out.println("Nota não encontrada");
                        }
                    } else {
                        System.out.println("Erro ao consultar: " + resultadoConsulta.get("message"));
                    }
                    break;
                    
                case 3:
                    System.out.println("Calculando média do aluno...");
                    
                    Map<String, Object> resultadoMediaAluno = chamarServicoNota("mediaAluno", alunoId, null, null);
                    
                    if ("success".equals(resultadoMediaAluno.get("status"))) {
                        Double media = (Double) resultadoMediaAluno.get("media");
                        if (media != null) {
                            System.out.println("Média do aluno ID " + alunoId + ": " + media);
                        } else {
                            System.out.println("Não foi possível calcular a média");
                        }
                    } else {
                        System.out.println("Erro ao calcular média: " + resultadoMediaAluno.get("message"));
                    }
                    break;
                    
                case 4:
                    System.out.println("Calculando média da disciplina...");
                    
                    Map<String, Object> resultadoMediaDisciplina = chamarServicoNota("mediaDisciplina", null, disciplinaId, null);
                    
                    if ("success".equals(resultadoMediaDisciplina.get("status"))) {
                        Double media = (Double) resultadoMediaDisciplina.get("media");
                        if (media != null) {
                            System.out.println("Média da disciplina ID " + disciplinaId + ": " + media);
                        } else {
                            System.out.println("Não foi possível calcular a média");
                        }
                    } else {
                        System.out.println("Erro ao calcular média: " + resultadoMediaDisciplina.get("message"));
                    }
                    break;
                    
                default:
                    System.out.println("Opção inválida, tente novamente.");
                    break;
            }

        } while (escolhaDoSubservico != 0);
    }

    //MENU HISTÓRICO 
    public static void menuHistorico(Integer escolhaDoSubservico, Scanner scanner){
        escolhaDoSubservico = -1;

        do{
            System.out.println("Qual subserviço você quer acessar?");
            System.out.println("1.Gerar histórico completo \n" +
            "2.Listar disciplinas aprovadas \n" +
            "3.Listar disciplinas reprovadas \n" +
            "4.Listar disciplinas em curso \n" +
            "5.Calcular coeficiente de rendimento \n" +
            "0.Voltar");

            escolhaDoSubservico = scanner.nextInt();

            if (escolhaDoSubservico == 0) {
                System.out.println("Voltando...");
                break;
            }

            System.out.println("Digite o ID do aluno: ");
            int alunoId = scanner.nextInt(); 

            switch (escolhaDoSubservico) {
                case 1:
                    System.out.println("Gerando histórico completo...");
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoHistorico = cliente.gerarHistoricoCompleto(alunoId);
                    
                    if ("success".equals(resultadoHistorico.get("status"))) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> historico = (List<Map<String, Object>>) resultadoHistorico.get("historico");
                        
                        if (historico.isEmpty()) {
                            System.out.println("Nenhum registro encontrado para o aluno ID " + alunoId);
                        } else {
                            System.out.println("Histórico do aluno ID " + alunoId + ":");
                            for (Map<String, Object> entrada : historico) {
                                System.out.println("Disciplina: " + entrada.get("disciplinaNome") + " | Nota: " + entrada.get("nota"));
                            }
                        }
                    } else {
                        System.out.println("Erro ao gerar histórico: " + resultadoHistorico.get("message"));
                    }
                    break;
                    
                case 2:
                    System.out.println("Listando disciplinas aprovadas...");
                    
                    Map<String, Object> resultadoAprovadas = chamarServicoHistorico("aprovadas", alunoId);
                    processarResultadoDisciplinas(resultadoAprovadas, "aprovadas", alunoId);
                    break;
                    
                case 3:
                    System.out.println("Listando disciplinas reprovadas...");
                    
                    Map<String, Object> resultadoReprovadas = chamarServicoHistorico("reprovadas", alunoId);
                    processarResultadoDisciplinas(resultadoReprovadas, "reprovadas", alunoId);
                    break;
                    
                case 4:
                    System.out.println("Listando disciplinas em curso...");
                    
                    Map<String, Object> resultadoEmCurso = chamarServicoHistorico("emCurso", alunoId);
                    
                    if ("success".equals(resultadoEmCurso.get("status"))) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> disciplinas = (List<Map<String, Object>>) resultadoEmCurso.get("disciplinas");
                        
                        if (disciplinas.isEmpty()) {
                            System.out.println("Nenhuma disciplina em curso encontrada para o aluno ID " + alunoId);
                        } else {
                            System.out.println("Disciplinas em curso do aluno ID " + alunoId + ":");
                            for (Map<String, Object> disciplina : disciplinas) {
                                System.out.println("Disciplina: " + disciplina.get("nome"));
                            }
                        }
                    } else {
                        System.out.println("Erro ao listar disciplinas em curso: " + resultadoEmCurso.get("message"));
                    }
                    break;
                    
                case 5:
                    System.out.println("Calculando coeficiente de rendimento...");
                    
                    Map<String, Object> resultadoCoeficiente = chamarServicoHistorico("coeficiente", alunoId);
                    
                    if ("success".equals(resultadoCoeficiente.get("status"))) {
                        Double coeficiente = (Double) resultadoCoeficiente.get("coeficiente");
                        if (coeficiente != null) {
                            System.out.println("Coeficiente de rendimento do aluno ID " + alunoId + ": " + coeficiente);
                        } else {
                            System.out.println("Não foi possível calcular o coeficiente de rendimento");
                        }
                    } else {
                        System.out.println("Erro ao calcular coeficiente: " + resultadoCoeficiente.get("message"));
                    }
                    break;
                    
                default:
                    System.out.println("Opção inválida, tente novamente.");
                    break;
            }

        } while (escolhaDoSubservico != 0);
    }
    
    //MÉTODOS AUXILIARES para chamar serviços específicos
    private static Map<String, Object> chamarServicoMatricula(String acao, Integer alunoId, Integer disciplinaId, Object extra) {
        try {
            return cliente.callService("matricula", criarRequest(acao, alunoId, disciplinaId, extra));
        } catch (Exception e) {
            System.err.println("Erro ao chamar serviço de matrícula: " + e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "Erro de comunicação: " + e.getMessage()
            );
            return errorResponse;
        }
    }
    
    private static Map<String, Object> chamarServicoNota(String acao, Integer alunoId, Integer disciplinaId, Object extra) {
        try {
            return cliente.callService("nota", criarRequest(acao, alunoId, disciplinaId, extra));
        } catch (Exception e) {
            System.err.println("Erro ao chamar serviço de nota: " + e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "Erro de comunicação: " + e.getMessage()
            );
            return errorResponse;
        }
    }
    
    private static Map<String, Object> chamarServicoHistorico(String acao, Integer alunoId) {
        try {
            return cliente.callService("historico", criarRequest(acao, alunoId, null, null));
        } catch (Exception e) {
            System.err.println("Erro ao chamar serviço de histórico: " + e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "Erro de comunicação: " + e.getMessage()
            );
            return errorResponse;
        }
    }
    
    private static Map<String, Object> criarRequest(String acao, Integer alunoId, Integer disciplinaId, Object extra) {
        Map<String, Object> request = new java.util.HashMap<>();
        request.put("action", acao);
        if (alunoId != null) request.put("alunoId", alunoId);
        if (disciplinaId != null) request.put("disciplinaId", disciplinaId);
        if (extra != null) request.put("extra", extra);
        return request;
    }
    
    private static void processarResultadoDisciplinas(Map<String, Object> resultado, String tipo, int alunoId) {
        if ("success".equals(resultado.get("status"))) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> disciplinas = (List<Map<String, Object>>) resultado.get("disciplinas");
            
            if (disciplinas.isEmpty()) {
                System.out.println("Nenhuma disciplina " + tipo + " encontrada para o aluno ID " + alunoId);
            } else {
                System.out.println("Disciplinas " + tipo + " do aluno ID " + alunoId + ":");
                for (Map<String, Object> entrada : disciplinas) {
                    System.out.println("Disciplina: " + entrada.get("disciplinaNome") + " | Nota: " + entrada.get("nota"));
                }
            }
        } else {
            System.out.println("Erro ao listar disciplinas " + tipo + ": " + resultado.get("message"));
        }
    }
}