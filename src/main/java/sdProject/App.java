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
        Integer escolhaDoServico; 

        do{
            System.out.println("SEJA BEM VINDO AO SISTEMA DE GERÊNCIAMENTO ACADÊMICO!");
            System.out.println("Digite seu nome: ");
            System.out.println("Qual serviço você quer acessar?(Digite um número de 1-4)");
            System.out.println("1.Matrícula \n2.Nota \n3.Histórico \n4.Sair");

            escolhaDoServico = scanner.nextInt();
            
            switch (escolhaDoServico) {
                case 1:
                    //menuMatricula();
                    break;
                case 2:
                    //menuNota();
                    break; 
                case 3:
                    //menuHistorico();
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

    public static void menuMatricula() {
        
    }

    public static void menuNota() {

    }

    public static void menuHistorico(){
        
    }
}
