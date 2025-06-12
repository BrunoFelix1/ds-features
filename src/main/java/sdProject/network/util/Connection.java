package sdProject.network.util;

import java.io.*;
import java.net.*;

public class Connection implements AutoCloseable {
    private final Socket tcpSocket;
    private final DatagramSocket udpSocket;
    private final ObjectInputStream input;
    private final ObjectOutputStream output;
    private final boolean isUDP;
    private static final int MAX_UDP_SIZE = 65507;

    public Connection(String host, int port) throws IOException {
        this.tcpSocket = new Socket(host, port);
        this.udpSocket = null;
        this.output = new ObjectOutputStream(tcpSocket.getOutputStream());
        this.input = new ObjectInputStream(tcpSocket.getInputStream());
        this.isUDP = false;
    }

    // Construtor para conexão TCP (Servidor)
    public Connection(Socket socket) throws IOException {
        this.tcpSocket = socket;
        this.udpSocket = null;
        this.output = new ObjectOutputStream(tcpSocket.getOutputStream());
        this.input = new ObjectInputStream(tcpSocket.getInputStream());
        this.isUDP = false;
    }

    // Construtor para conexão UDP (Servidor)
    public Connection(int port) throws IOException {
        this.tcpSocket = null;
        this.udpSocket = new DatagramSocket(port);
        this.output = null;
        this.input = null;
        this.isUDP = true;
    }

    // Construtor para conexão UDP (Cliente)
    public Connection() throws IOException {
        this.tcpSocket = null;
        this.udpSocket = new DatagramSocket();
        this.output = null;
        this.input = null;
        this.isUDP = true;
    }

    public void send(Object obj) throws IOException {
        if (isUDP) {
            throw new UnsupportedOperationException("Para UDP, use sendUDP(Object, InetAddress, int)");
        }
        byte[] serializedData = SerializationUtils.serialize(obj);
        output.writeInt(serializedData.length);
        output.write(serializedData);
        output.flush();
    }

    public void sendUDP(Object obj, InetAddress address, int port) throws IOException {
        if (!isUDP) {
            throw new UnsupportedOperationException("Este método só pode ser usado com conexões UDP");
        }
        byte[] serializedData = SerializationUtils.serialize(obj);
        DatagramPacket packet = new DatagramPacket(serializedData, serializedData.length, address, port);
        udpSocket.send(packet);
    }

    public Object receive() throws IOException, ClassNotFoundException {
        if (isUDP) {
            throw new UnsupportedOperationException("Para UDP, use receiveUDP()");
        }
        int length = input.readInt();
        byte[] data = new byte[length];
        input.readFully(data);
        return SerializationUtils.deserialize(data);
    }

    public DatagramPacket receiveUDP() throws IOException {
        if (!isUDP) {
            throw new UnsupportedOperationException("Este método só pode ser usado com conexões UDP");
        }
        byte[] buffer = new byte[MAX_UDP_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        udpSocket.receive(packet);
        return packet;
    }

    public void setSoTimeout(int timeout) throws SocketException {
        if (isUDP) {
            udpSocket.setSoTimeout(timeout);
        } else {
            tcpSocket.setSoTimeout(timeout);
        }
    }

    public Socket getTcpSocket() {
        return tcpSocket;
    }

    public DatagramSocket getUdpSocket() {
        return udpSocket;
    }

    @Override
    public void close() throws IOException {
        if (isUDP) {
            if (udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.close();
            }
        } else {
            if (input != null) input.close();
            if (output != null) output.close();
            if (tcpSocket != null) tcpSocket.close();
        }
    }
}