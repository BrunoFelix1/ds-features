package sdProject.network.monitor;

public class WorkerInfo {
    String serviceType;
    String className;
    int initialPort;
    
    WorkerInfo(String serviceType, String className, int initialPort) {
        this.serviceType = serviceType;
        this.className = className;
        this.initialPort = initialPort;
    }
}
