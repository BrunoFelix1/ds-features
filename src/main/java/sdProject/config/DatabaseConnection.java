package sdProject.config;

import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {    private static Connection connection = null;    public static Connection getConnection() throws SQLException {        if (connection == null || connection.isClosed()) {
            String url = AppConfig.getDbUrl();
            String username = AppConfig.getDbUsername();
            String password = AppConfig.getDbPassword();

            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }    public static void runMigrations() {
        String url = AppConfig.getDbUrl();
        String username = AppConfig.getDbUsername();
        String password = AppConfig.getDbPassword();

        Flyway flyway = Flyway.configure()
                .dataSource(url, username, password)
                .locations(AppConfig.getFlywayLocations())
                .load();

        flyway.migrate();
        System.out.println("Migrações aplicadas com sucesso!");
    }

    //Fecha conexão
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Conexão com banco de dados fechada.");
                }
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}