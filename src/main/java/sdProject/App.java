package sdProject;
import java.util.Scanner; 
import sdProject.config.DatabaseConnection;

public class App {
    public static void main(String[] args) {

        System.out.println("Iniciando aplicação");
        
        // try {
        //     System.out.println("Executando migrações do banco de dados...");
        //     DatabaseConnection.runMigrations();
        //     System.out.println("Migrações concluídas com sucesso!");
            
        // } catch (Exception e) {
        //     System.err.println("Erro ao executar migrações: " + e.getMessage());
        //     e.printStackTrace();
        // } finally {
        //     System.out.println("Aplicação finalizada");
        // }

        //Menu do nosso sistema
        Scanner scanner = new Scanner(System.in);
        Integer escolhaDoServico = -1;

        do{
            System.out.println("SEJA BEM VINDO AO SISTEMA DE GERÊNCIAMENTO ACADÊMICO!");
            System.out.println("Qual serviço você quer acessar?(Digite um número de 1-4)");
            System.out.println("1.Matrícula \n2.Nota \n3.Histórico \n4.Sair");

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

    //Chamando as subopções de cada serviço 

    public static void menuMatricula(Integer escolhaDoSubservico, Scanner scanner) {
        escolhaDoSubservico = -1;
        
        do{
            System.out.println("Qual subserviço você quer acessar?");
            System.out.println("1.Verificar matrícula \n" +
            "2.Listar matrículas por aluno \n" +
            "3.Listar matrículas por disciplina \n" +
            "0.Voltar");

            escolhaDoSubservico = scanner.nextInt();
    
            switch (escolhaDoSubservico) {
                case 1:
                    System.out.println("Verificando matrícula...");
                    //Chamar o serviço
                    break;
                case 2:
                    System.out.println("Listando matrículas por aluno...");
                    break;
                case 3:
                    System.out.println("Listando matrículas por disciplina...");
                    break;
                case 0:
                    System.out.println("Voltando...");
                    break;
                default:
                    System.out.println("Opção inválida, tente novamente.");
            }
        } while (escolhaDoSubservico != 0);

    }

    public static void menuNota(Integer escolhaDoSubservico, Scanner scanner) {
        escolhaDoSubservico = -1;

        do{
            System.out.println("Qual subserviço você quer acessar?");
            System.out.println("1.Consultar nota \n" +
            "2.Calcular média aluno \n" +
            "3.Calcular média disciplina \n" +
            "0.Voltar");

            escolhaDoSubservico = scanner.nextInt();

            switch (escolhaDoSubservico) {
                case 1:
                    System.out.println("Consultando nota...");
                    break;
                case 2:
                    System.out.println("Calculando média do aluno...");
                    break;
                case 3:
                    System.out.println("Calculando média da disciplina...");
                    break;
                case 0:
                    System.out.println("Voltando...");
                    break;
            
                default:
                    System.out.println("Opção inválida, tente novamente.");
                    break;
            }

        } while (escolhaDoSubservico != 0);
           
    }

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

            switch (escolhaDoSubservico) {
                case 1:
                    System.out.println("Gerando histórico completo...");
                    break;
                case 2:
                    System.out.println("Listando disciplinas aprovadas...");
                    break;
                case 3:
                    System.out.println("Listando disciplinas reprovadas...");
                    break;
                case 4:
                    System.out.println("Listando disciplinas em curso...");
                    break;
                case 5:
                    System.out.println("Calculando coeficiente de rendimento...");
                    break;
                case 0:
                     System.out.println("Voltando...");
                    break;
                default:
                    System.out.println("Opção inválida, tente novamente.");
                    break;
            }

        } while (escolhaDoSubservico != 0);

        
    }
}
