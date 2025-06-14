package sdProject.network.client;

//classe simples, pra saber onde tá um worker
public class ServiceLocation {
    private final String host;
    private final int port; 
    
    public ServiceLocation(String address) {
        String[] parts = address.split(":");
        this.host = parts[0];
        this.port = Integer.parseInt(parts[1]);
    }
    
    public ServiceLocation(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    @Override
    public String toString() {
        return host + ":" + port;
    }
}
