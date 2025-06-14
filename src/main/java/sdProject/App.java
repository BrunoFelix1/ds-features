package sdProject;


import sdProject.config.DatabaseConnection;

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

    }

    
}