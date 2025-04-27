package sdProject.network.client;

public class ServiceLocation {
    private final String host;
    private final int port;
    
    public ServiceLocation(String location) {
        String[] parts = location.split(":");
        this.host = parts[0];
        this.port = Integer.parseInt(parts[1]);
    }
    
    public String getHost() { return host; }
    public int getPort() { return port; }
}
