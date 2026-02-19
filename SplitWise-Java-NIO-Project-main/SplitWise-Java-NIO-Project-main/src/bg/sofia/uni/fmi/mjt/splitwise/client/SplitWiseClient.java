package bg.sofia.uni.fmi.mjt.splitwise.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SplitWiseClient {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 4096;

    public void start() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the SplitWise server.");

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (true) {
                System.out.print("$ ");
                String command = scanner.nextLine();

                if ("quit".equals(command)) {
                    break;
                }

                if (command.isEmpty()) {
                    continue;
                }

                buffer.clear();
                buffer.put(command.getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                socketChannel.write(buffer);

                buffer.clear();
                int bytesRead = socketChannel.read(buffer);
                if (bytesRead == -1) {
                    System.out.println("Server disconnected.");
                    break;
                }

                buffer.flip();
                String reply = StandardCharsets.UTF_8.decode(buffer).toString();
                System.out.println(reply);
            }

        } catch (IOException e) {
            System.err.println("There is a problem with the network communication: " + e.getMessage());
        }
    }

    static void main() {
        new SplitWiseClient().start();
    }
}