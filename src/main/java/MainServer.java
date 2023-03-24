import java.io.*;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

public class MainServer {

    public static final int SERVER_PORT = 50001;
    private static final Queue<Client> clients = new ConcurrentLinkedQueue<>();
    private static final Set<String> identifiers = new ConcurrentSkipListSet<>();
    private static final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
    private static volatile boolean isRunning = true;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> isRunning = false));

        try (final var server = new ServerSocket(SERVER_PORT);
             final var senderThread = new MessageSenderThread(clients, messages)) {
            senderThread.start();

            do {
                var clientConnection = server.accept();
                var client = new Client(clientConnection, null);
                var thread = new ClientHandlerThread(client, clients, messages, identifiers);
                thread.start();
            } while (isRunning);
        } catch (SocketException e) {
            System.out.println("Client disconnected");
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
