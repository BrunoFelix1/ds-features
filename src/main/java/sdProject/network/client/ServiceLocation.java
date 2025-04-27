package sdProject.network.client;


//isso aqui é praticamente uma classe de utils mesmo
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
