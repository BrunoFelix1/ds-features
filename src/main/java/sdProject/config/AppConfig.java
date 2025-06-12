package sdProject.config;

import java.io.*;
import java.util.Properties;
import java.util.List;
import java.util.Arrays;

public class AppConfig {
    private static final Properties properties = new Properties();
    
    static {
        loadConfig();
    }
    
    private static void loadConfig() {
        // Tenta carregar o arquivo .env primeiro
        try (InputStream envStream = new FileInputStream(".env")) {
            properties.load(envStream);
            System.out.println("Configurações carregadas do arquivo .env");
        } catch (IOException e) {
            System.out.println("Arquivo .env não encontrado, carregando valores padrão");
            loadDefaults();
        }
        
        // Sobrescreve com variáveis de ambiente se existirem
        overrideWithEnvironmentVariables();
    }
    
    private static void loadDefaults() {
        // Gateway
        properties.setProperty("GATEWAY_HOST", "localhost");
        properties.setProperty("GATEWAY_PORT", "8080");
        
        // Workers
        properties.setProperty("MATRICULA_WORKER_PORT", "8081");
        properties.setProperty("NOTA_WORKER_PORT", "8082");
        properties.setProperty("HISTORICO_WORKER_PORT", "8083");
        
        // Remote Agents
        properties.setProperty("REMOTE_AGENT_PORT", "9000");
        properties.setProperty("REMOTE_AGENTS", "localhost:9000,localhost:9001,localhost:9002");
        
        // Timeouts e Intervalos
        properties.setProperty("UDP_TIMEOUT_MS", "5000");
        properties.setProperty("HEARTBEAT_INTERVAL_SECONDS", "10");
        properties.setProperty("HEALTH_CHECK_INTERVAL_SECONDS", "10");
        
        // Thread Pools
        properties.setProperty("GATEWAY_THREAD_POOL_SIZE", "10");
        properties.setProperty("WORKER_THREAD_POOL_SIZE", "10");
        
        // Database
        properties.setProperty("DB_URL", "jdbc:postgresql://localhost:5432/sd_project");
        properties.setProperty("DB_USERNAME", "bruno_upe");
        properties.setProperty("DB_PASSWORD", "123");
        
        // Flyway
        properties.setProperty("FLYWAY_URL", "jdbc:postgresql://localhost:5432/sd_project");
        properties.setProperty("FLYWAY_USER", "bruno_upe");
        properties.setProperty("FLYWAY_PASSWORD", "123");
        properties.setProperty("FLYWAY_LOCATIONS", "classpath:db/migrations");
    }
    
    private static void overrideWithEnvironmentVariables() {
        // Permite sobrescrever com variáveis de ambiente do sistema
        for (String key : properties.stringPropertyNames()) {
            String envValue = System.getenv(key);
            if (envValue != null) {
                properties.setProperty(key, envValue);
                System.out.println("Configuração " + key + " sobrescrita pela variável de ambiente");
            }
        }
    }
    
    // Métodos para Gateway
    public static String getGatewayHost() {
        return properties.getProperty("GATEWAY_HOST");
    }
    
    public static int getGatewayPort() {
        return Integer.parseInt(properties.getProperty("GATEWAY_PORT"));
    }
    
    // Métodos para Workers
    public static int getMatriculaWorkerPort() {
        return Integer.parseInt(properties.getProperty("MATRICULA_WORKER_PORT"));
    }
    
    public static int getNotaWorkerPort() {
        return Integer.parseInt(properties.getProperty("NOTA_WORKER_PORT"));
    }
    
    public static int getHistoricoWorkerPort() {
        return Integer.parseInt(properties.getProperty("HISTORICO_WORKER_PORT"));
    }
    
    // Métodos para Remote Agents
    public static int getRemoteAgentPort() {
        return Integer.parseInt(properties.getProperty("REMOTE_AGENT_PORT"));
    }
    
    public static List<String> getRemoteAgents() {
        String agentsStr = properties.getProperty("REMOTE_AGENTS");
        return Arrays.asList(agentsStr.split(","));
    }
    
    // Métodos para Timeouts e Intervalos
    public static int getUdpTimeoutMs() {
        return Integer.parseInt(properties.getProperty("UDP_TIMEOUT_MS"));
    }
    
    public static int getHeartbeatIntervalSeconds() {
        return Integer.parseInt(properties.getProperty("HEARTBEAT_INTERVAL_SECONDS"));
    }
    
    public static int getHealthCheckIntervalSeconds() {
        return Integer.parseInt(properties.getProperty("HEALTH_CHECK_INTERVAL_SECONDS"));
    }
    
    // Métodos para Thread Pools
    public static int getGatewayThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("GATEWAY_THREAD_POOL_SIZE"));
    }
    
    public static int getWorkerThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("WORKER_THREAD_POOL_SIZE"));
    }
    
    // Métodos para Database
    public static String getDbUrl() {
        return properties.getProperty("DB_URL");
    }
    
    public static String getDbUsername() {
        return properties.getProperty("DB_USERNAME");
    }
    
    public static String getDbPassword() {
        return properties.getProperty("DB_PASSWORD");
    }
    
    // Métodos para Flyway
    public static String getFlywayUrl() {
        return properties.getProperty("FLYWAY_URL");
    }
    
    public static String getFlywayUser() {
        return properties.getProperty("FLYWAY_USER");
    }
    
    public static String getFlywayPassword() {
        return properties.getProperty("FLYWAY_PASSWORD");
    }
    
    public static String getFlywayLocations() {
        return properties.getProperty("FLYWAY_LOCATIONS");
    }
    
    // Método genérico para obter qualquer propriedade
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    // Método para debug - mostrar todas as configurações
    public static void printAllConfigs() {
        System.out.println("=== Configurações Atuais ===");
        properties.forEach((key, value) -> {
            // Não mostra senhas
            if (key.toString().toLowerCase().contains("password")) {
                System.out.println(key + " = [HIDDEN]");
            } else {
                System.out.println(key + " = " + value);
            }
        });
        System.out.println("============================");
    }
}
