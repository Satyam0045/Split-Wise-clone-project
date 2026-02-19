package bg.sofia.uni.fmi.mjt.splitwise.server;

import bg.sofia.uni.fmi.mjt.splitwise.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;
import bg.sofia.uni.fmi.mjt.splitwise.util.ErrorLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class SplitWiseServer {
    private static final int PORT = 7777;
    private static final int BUFFER_SIZE = 4096;
    private final boolean isRunning = true;
    private Selector selector;
    private final CommandExecutor commandExecutor;

    public SplitWiseServer(DataRepository repository) {
        this.commandExecutor = new CommandExecutor(repository);
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("SplitWise Server started on port " + PORT);

            while (isRunning) {
                int readyChannels = selector.select();
                if (readyChannels == 0) continue;

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isReadable()) {
                        handleRead(key);
                    } else if (key.isAcceptable()) {
                        handleAccept(key);
                    }

                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            ErrorLogger.log(e);
            e.printStackTrace();
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(BUFFER_SIZE));
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        int bytesRead;
        try {
            bytesRead = clientChannel.read(buffer);
        } catch (IOException e) {
            clientChannel.close();
            return;
        }

        if (bytesRead == -1) {
            clientChannel.close();
            return;
        }

        buffer.flip();
        String message = StandardCharsets.UTF_8.decode(buffer).toString().strip();
        buffer.clear();

        if (message.isEmpty()) {
            return;
        }

        String response = commandExecutor.execute(message, key);

        if (response != null) {
            buffer.put((response + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            buffer.flip();
            clientChannel.write(buffer);
            buffer.clear();
        }
    }

    static void main() {
        new SplitWiseServer(new DataRepository()).start();
    }
}