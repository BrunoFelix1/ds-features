package sdProject.network.client;

import sdProject.config.AppConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class NotaLoadTest {
    public static void main(String[] args) throws Exception {
        String gatewayHost = AppConfig.getGatewayHost();
        int gatewayPort = AppConfig.getGatewayPort();
        int numThreads = 50; // Quantidade de requisições simultâneas
        int alunoId = 1; // IDs fixos para o teste
        int disciplinaId = 1;
        double nota = 8.5;

        if (args.length >= 3) {
            gatewayHost = args[0];
            gatewayPort = Integer.parseInt(args[1]);
            numThreads = Integer.parseInt(args[2]);
        }

        Cliente cliente = new Cliente(gatewayHost, gatewayPort);
        CountDownLatch latch = new CountDownLatch(numThreads);

        long start = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    Map<String, Object> request = new HashMap<>();
                    request.put("action", "registrarNota");
                    request.put("alunoId", alunoId);
                    request.put("disciplinaId", disciplinaId);
                    request.put("nota", nota);
                    Map<String, Object> resp = cliente.callService("nota", request);
                    System.out.println("Thread " + Thread.currentThread().getId() + " resposta: " + resp);
                } catch (Exception e) {
                    System.err.println("Thread " + Thread.currentThread().getId() + " erro: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("Teste de carga finalizado. Tempo total: " + (end - start) + " ms");
    }
}
