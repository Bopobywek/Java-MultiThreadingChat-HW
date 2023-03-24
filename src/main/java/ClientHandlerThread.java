import java.io.*;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class ClientHandlerThread extends Thread {
    private final Client client;
    private final Queue<Client> clients;
    private final BlockingQueue<Message> messages;
    private final Set<String> identifiers;
    private volatile boolean isRunning = true;

    public ClientHandlerThread(Client client, Queue<Client> clients, BlockingQueue<Message> messages,
                               Set<String> identifiers) {
        this.client = client;
        this.clients = clients;
        this.messages = messages;
        this.identifiers = identifiers;
    }

    @Override
    public void run() {
        try (final var inputStream = client.getSocket().getInputStream();
             final var outputStream = client.getSocket().getOutputStream();
             final var reader = new InputStreamReader(inputStream);
             final var writer = new DataOutputStream(outputStream)) {

            final var bufferedReader = new BufferedReader(reader);
            String receivedData;
            writer.writeBytes("Input your username:\n");
            do {
                receivedData = bufferedReader.readLine();
                if (receivedData == null || receivedData.equals("!exit")) {
                    sendExitMessage();
                    return;
                }
                if (client.getName() == null && !identifiers.contains(receivedData)) {
                    identifiers.add(receivedData);
                    client.setName(receivedData);
                    clients.add(client);

                    var message = new Message(client, MessageType.NEW_USER, MessageFormat.format("New" +
                            " user {0}", client.getName()));
                    messages.add(message);
                } else if (client.getName() == null) {
                    writer.writeBytes("User wth this name already exist\n");
                } else {
                    var message = new Message(client, MessageType.MESSAGE, receivedData);
                    messages.add(message);
                }
            } while (isRunning);
        } catch (SocketException e) {
            sendExitMessage();
        } catch (Exception e) {
            System.out.println(Thread.currentThread().getName() + " exception");
        }
    }

    public void sendExitMessage() {
        clients.remove(client);
        identifiers.remove(client.getName());
        var message = new Message(
                client,
                MessageType.USER_EXIT,
                MessageFormat.format("{0} left", client.getName()));
        messages.add(message);
        isRunning = false;
    }

    public void close() {
        isRunning = false;
    }
}
