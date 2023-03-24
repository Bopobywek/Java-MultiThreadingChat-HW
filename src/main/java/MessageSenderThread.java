import java.io.*;
import java.text.MessageFormat;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class MessageSenderThread extends Thread implements AutoCloseable {
    private final Queue<Client> clients;
    private final BlockingQueue<Message> messages;

    private volatile boolean isRunning = true;

    public MessageSenderThread(Queue<Client> clients, BlockingQueue<Message> messages) {
        this.clients = clients;
        this.messages = messages;
    }

    @Override
    public void run() {
        do {
            Message message;
            try {
                message = messages.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("interrupted");
                return;
            }

            for (var client : clients) {
                try {
                    var outputStream = client.getSocket().getOutputStream();
                    var writer = new DataOutputStream(outputStream);

                    if (message.type() == MessageType.MESSAGE) {
                        writer.writeBytes(MessageFormat.format("[{0}]: {1}\n", message.from().getName(),
                                message.content()));
                    } else {
                        writer.writeBytes(MessageFormat.format("[CHAT]: {0}\n", message.content()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } while (isRunning);
    }

    @Override
    public void close() {
        isRunning = false;
    }
}
