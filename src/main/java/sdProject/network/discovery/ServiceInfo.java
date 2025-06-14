package sdProject.network.discovery;

public class ServiceInfo {
    public final String address;
    public final String serviceType;
    public final String instanceId;
    public volatile long lastHeartbeat;
    
    public ServiceInfo(String address, String serviceType, String instanceId) {
        this.address = address;
        this.serviceType = serviceType;
        this.instanceId = instanceId;
        this.lastHeartbeat = System.currentTimeMillis();
    }
    
    // Construtor para compatibilidade com código existente
    public ServiceInfo(String address, String serviceType) {
        this(address, serviceType, serviceType + "-" + address);
    }
}
