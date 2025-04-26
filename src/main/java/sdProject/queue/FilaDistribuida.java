package sdProject.queue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilaDistribuida {

    private final int httpPort = 8080; // Porta para receber requisições HTTP
    private final ExecutorService threadPool = Executors.newFixedThreadPool(5); // Paralelismo para lidar com req, para não ocorrer a "trava", do exemplo da primeira aula de SD

    public void iniciarEndpointHTTP() {
        try (ServerSocket serverSocket = new ServerSocket(httpPort)) {
            System.out.println("Endpoint HTTP iniciado na porta " + httpPort);
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Aguarda conexões de clientes
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.run();
                threadPool.submit(new ClientHandler(clientSocket)); // Delega o tratamento para uma thread criada na linha 12
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o endpoint HTTP: " + e.getMessage());
        }
    }
}