package sdProject.network.discovery;
// classe util para a gente saber coisas tipo
// o endereço do worker, o tipo de serviço e o último heartbeat
// que ele enviou. Isso é útil para o WorkerMonitor/gd saber se o worker está ativo ou não
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
