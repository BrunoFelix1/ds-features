package sdProject;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import sdProject.config.DatabaseConnection;
import sdProject.models.Disciplina;
import sdProject.models.Matricula;
import sdProject.services.impl.HistoricoServiceImpl;
import sdProject.services.impl.MatriculaServiceImpl;
import sdProject.services.impl.NotaServiceImpl;
import sdProject.services.interfaces.HistoricoService;
import sdProject.services.interfaces.MatriculaService;
import sdProject.services.interfaces.NotaService;

public class App {
    public static void main(String[] args) {

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

    //Funções das subopções de cada serviço 

    public static void menuMatricula(Integer escolhaDoSubservico, Scanner scanner) {
        escolhaDoSubservico = -1;

        
        try {
            // Instanciando a implementação
            MatriculaService matriculaService = new MatriculaServiceImpl();

            do{
                System.out.println("Qual subserviço você quer acessar?");
                System.out.println("1.Verificar matrícula \n" +
                "2.Listar matrículas por aluno \n" +
                "3.Listar matrículas por disciplina \n" +
                "0.Voltar");
    
                escolhaDoSubservico = scanner.nextInt();
    
                System.out.println("Digite o ID do aluno: ");
                int alunoId = scanner.nextInt();
                System.out.println("Digite o ID da disciplina: ");
                int disciplinaId = scanner.nextInt();
        
                switch (escolhaDoSubservico) {
                    case 1:
                        System.out.println("Verificando matrícula do aluno:" + alunoId + " . Na disciplina:" + disciplinaId);
                        
                        boolean resultado = matriculaService.verificarMatricula(alunoId, disciplinaId);
                        System.out.println("Aluno matriculado? " + resultado);
    
                        break;
                    case 2:
                        System.out.println("Listando matrículas do aluno:" + alunoId);
    
                        List<Matricula> matriculasPorAluno = matriculaService.buscarMatriculasPorAluno(alunoId);
                        // Exibindo as matrículas
                        if (matriculasPorAluno.isEmpty()) {
                            System.out.println("Nenhuma matrícula encontrada para o aluno ID " + alunoId);
                        } else {
                            for (Matricula matricula : matriculasPorAluno) {
                                System.out.println("Disciplina ID: " + matricula.getDisciplinaId() + " | Nota: " + matricula.getNota());
                            }
                        }
    
                        break;
                    case 3:
                        System.out.println("Listando matrículas por disciplina...");

                        // Chamando o método para buscar matrículas por disciplina
                        List<Matricula> matriculasPorDisciplina = matriculaService.buscarMatriculasPorDisciplina(disciplinaId);

                        // Exibindo as matrículas encontradas
                        if (matriculasPorDisciplina.isEmpty()) {
                            System.out.println("Nenhuma matrícula encontrada para a disciplina ID " + disciplinaId);
                        } else {
                            System.out.println("Matrículas da disciplina ID " + disciplinaId + ":");
                            for (Matricula matricula : matriculasPorDisciplina) {
                                System.out.println("Aluno ID: " + matricula.getAlunoId() + " | Nota: " + matricula.getNota());
                            }
                        }

                        break;
                    case 0:
                        System.out.println("Voltando...");
                        break;
                    default:
                        System.out.println("Opção inválida, tente novamente.");
                }
            } while (escolhaDoSubservico != 0);

        } catch (SQLException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        
    }

    public static void menuNota(Integer escolhaDoSubservico, Scanner scanner) {
        escolhaDoSubservico = -1;

        try {

            // Instanciando a implementação
            NotaService notaService = new NotaServiceImpl();

            do{
                System.out.println("Qual subserviço você quer acessar?");
                System.out.println("1.Consultar nota \n" +
                "2.Calcular média aluno \n" +
                "3.Calcular média disciplina \n" +
                "0.Voltar");
    
                escolhaDoSubservico = scanner.nextInt();

                System.out.println("Digite o ID do aluno: ");
                int alunoId = scanner.nextInt();
                System.out.println("Digite o ID da disciplina: ");
                int disciplinaId = scanner.nextInt(); 
    
                switch (escolhaDoSubservico) {
                    case 1:
                        System.out.println("Consultando nota...");

                        // Chamando o método para consultar nota
                        Double nota = notaService.consultarNota(alunoId, disciplinaId);

                        // Exibindo resultado ao usuário
                        if (nota != null) {
                            System.out.println("Nota do aluno " + alunoId + " na disciplina " + disciplinaId + ": " + nota);
                        } else {
                            System.out.println("Nota não encontrada para o aluno ID " + alunoId + " na disciplina ID " + disciplinaId);
                        }

                        break;
                    case 2:
                        System.out.println("Calculando média do aluno...");

                        // Chamando o método para calcular a média do aluno
                        Double mediaDoAluno = notaService.calcularMediaAluno(alunoId);

                        // Exibindo resultado ao usuário
                        if (mediaDoAluno != null) {
                            System.out.println("Média do aluno ID " + alunoId + ": " + mediaDoAluno);
                        } else {
                            System.out.println("Não foi possível calcular a média para o aluno ID " + alunoId);
                        }

                        break;
                    case 3:
                        System.out.println("Calculando média da disciplina...");

                        // Chamando o método para calcular a média da disciplina
                        Double mediaDaDisciplina = notaService.calcularMediaDisciplina(disciplinaId);

                        // Exibindo resultado ao usuário
                        if (mediaDaDisciplina != null) {
                            System.out.println("Média da disciplina ID " + disciplinaId + ": " + mediaDaDisciplina);
                        } else {
                            System.out.println("Não foi possível calcular a média para a disciplina ID " + disciplinaId);
                        }

                        break;
                    case 0:
                        System.out.println("Voltando...");
                        break;
                
                    default:
                        System.out.println("Opção inválida, tente novamente.");
                        break;
                }
    
            } while (escolhaDoSubservico != 0);

        } catch (SQLException e) {
            // TODO: handle exception
            e.printStackTrace();
        }

           
    }

    public static void menuHistorico(Integer escolhaDoSubservico, Scanner scanner){
        escolhaDoSubservico = -1;

        try {
            // Instanciando a implementação
            HistoricoService historicoService = new HistoricoServiceImpl();

            do{
    
                System.out.println("Qual subserviço você quer acessar?");
                System.out.println("1.Gerar histórico completo \n" +
                "2.Listar disciplinas aprovadas \n" +
                "3.Listar disciplinas reprovadas \n" +
                "4.Listar disciplinas em curso \n" +
                "5.Calcular coeficiente de rendimento \n" +
                "0.Voltar");
    
                escolhaDoSubservico = scanner.nextInt();

                System.out.println("Digite o ID do aluno: ");
                int alunoId = scanner.nextInt(); 
    
                switch (escolhaDoSubservico) {
                    case 1:
                        System.out.println("Gerando histórico completo...");

                        // Chamando o método para gerar o histórico completo
                        Map<Disciplina, Double> historico = historicoService.gerarHistoricoCompleto(alunoId);

                        // Exibindo o histórico do aluno
                        if (historico.isEmpty()) {
                            System.out.println("Nenhum registro encontrado para o aluno ID " + alunoId);
                        } else {
                            System.out.println("Histórico do aluno ID " + alunoId + ":");
                            for (Map.Entry<Disciplina, Double> entry : historico.entrySet()) {
                                System.out.println("Disciplina: " + entry.getKey().getNome() + " | Nota: " + entry.getValue());
                            }
                        }

                        break;
                    case 2:
                        System.out.println("Listando disciplinas aprovadas...");

                        // Chamando o método para listar disciplinas aprovadas
                        Map<Disciplina, Double> disciplinasAprovadas = historicoService.listarDisciplinasAprovadas(alunoId);

                        // Exibindo disciplinas aprovadas do aluno
                        if (disciplinasAprovadas.isEmpty()) {
                            System.out.println("Nenhuma disciplina aprovada encontrada para o aluno ID " + alunoId);
                        } else {
                            System.out.println("Disciplinas aprovadas do aluno ID " + alunoId + ":");
                            for (Map.Entry<Disciplina, Double> entry : disciplinasAprovadas.entrySet()) {
                                System.out.println("Disciplina: " + entry.getKey().getNome() + " | Nota: " + entry.getValue());
                            }
                        }

                        break;
                    case 3:
                        System.out.println("Listando disciplinas reprovadas...");

                        // Chamando o método para listar disciplinas reprovadas
                        Map<Disciplina, Double> disciplinasReprovadas = historicoService.listarDisciplinasReprovadas(alunoId);

                        // Exibindo disciplinas reprovadas do aluno
                        if (disciplinasReprovadas.isEmpty()) {
                            System.out.println("Nenhuma disciplina reprovada encontrada para o aluno ID " + alunoId);
                        } else {
                            System.out.println("Disciplinas reprovadas do aluno ID " + alunoId + ":");
                            for (Map.Entry<Disciplina, Double> entry : disciplinasReprovadas.entrySet()) {
                                System.out.println("Disciplina: " + entry.getKey().getNome() + " | Nota: " + entry.getValue());
                            }
                        }

                        break;
                    case 4:
                        System.out.println("Listando disciplinas em curso...");

                        // Chamando o método para listar disciplinas em curso
                        List<Disciplina> disciplinasEmCurso = historicoService.listarDisciplinasEmCurso(alunoId);

                        // Exibindo disciplinas em curso do aluno
                        if (disciplinasEmCurso.isEmpty()) {
                            System.out.println("Nenhuma disciplina em curso encontrada para o aluno ID " + alunoId);
                        } else {
                            System.out.println("Disciplinas em curso do aluno ID " + alunoId + ":");
                            for (Disciplina disciplina : disciplinasEmCurso) {
                                System.out.println("Disciplina: " + disciplina.getNome());
                            }
                        }

                        break;
                    case 5:
                        System.out.println("Calculando coeficiente de rendimento...");

                        // Chamando o método para calcular o coeficiente de rendimento
                        Double coeficienteRendimento = historicoService.calcularCoeficienteRendimento(alunoId);

                        // Exibindo resultado ao usuário
                        if (coeficienteRendimento != null) {
                            System.out.println("Coeficiente de rendimento do aluno ID " + alunoId + ": " + coeficienteRendimento);
                        } else {
                            System.out.println("Não foi possível calcular o coeficiente de rendimento para o aluno ID " + alunoId);
                        }

                            break;
                        case 0:
                            System.out.println("Voltando...");
                            break;
                        default:
                            System.out.println("Opção inválida, tente novamente.");

                        break;
                }
    
            } while (escolhaDoSubservico != 0);
            
        } catch (Exception e) {
            // TODO: handle exception
        }


        
    }
}

