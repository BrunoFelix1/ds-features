package sdProject.network.discovery;

public class ServiceInfo {
    String address;
    String serviceType;
    long lastHeartbeat;
    
    ServiceInfo(String address, String serviceType) {
        this.address = address;
        this.serviceType = serviceType;
        this.lastHeartbeat = System.currentTimeMillis();
    }
}
