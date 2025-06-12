package sdProject.network.workers.monitor;
//classe util para saber informações sobre os workers
// tipo a porta inicial, o tipo de serviço e a classe que implementa o worker, pra poder compilar
// e subir de novo o worker caso ele caia
public class WorkerInfo {
    String serviceType;
    String className;
    int initialPort;
    
    public WorkerInfo(String serviceType, String className, int initialPort) {
        this.serviceType = serviceType;
        this.className = className;
        this.initialPort = initialPort;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public String getClassName() {
        return className;
    }
    
    public int getInitialPort() {
        return initialPort;
    }
}
