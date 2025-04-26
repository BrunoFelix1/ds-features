package sdProject.queue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    private void enviarRespostaHTTP(PrintWriter out, String status, String contentType, String body) {
        out.println("HTTP/1.1 " + status);
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + body.length());
        out.println(); // Linha em branco para separar os headers do body
        out.println(body);
        out.flush();
    }

    private void processarRequisicaoHTTP(String metodo, String path, BufferedReader in, PrintWriter out) throws IOException {
        System.out.println("Recebida requisição: " + metodo + " " + path);

        StringBuilder requestBody = new StringBuilder();
        if (metodo.equalsIgnoreCase("POST")) {
            int contentLength = 0;
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
                }
                System.out.println("Header: " + line); // Para debug
            }

            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                in.read(bodyChars, 0, contentLength);
                requestBody.append(bodyChars);
                System.out.println("Corpo da requisição para " + path + ": " + requestBody.toString());
                enviarRespostaHTTP(out, "200 OK", "text/plain", requestBody.toString());
            }
        }


    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            // Obtém o método e o path a partir da leitura da primeira linha da requisição
            String requestLine = in.readLine();
            if (requestLine != null) {
                String[] parts = requestLine.split(" ");
                if (parts.length == 3) {
                    String metodo = parts[0];
                    String path = parts[1];
                    processarRequisicaoHTTP(metodo, path, in, out);
                } else {
                    enviarRespostaHTTP(out, "400 Bad Request", "text/plain", "Requisição HTTP inválida");
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao lidar com cliente: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar socket do cliente: " + e.getMessage());
            }
        }
    }

}
