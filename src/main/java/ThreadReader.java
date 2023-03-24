import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ThreadReader extends Thread implements AutoCloseable {

    private final Socket socket;

    private volatile boolean isRunning = true;

    public ThreadReader(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (final var reader = new InputStreamReader(socket.getInputStream())) {
            final var bufferedReader = new BufferedReader(reader);
            String receivedData;
            do {
                receivedData = bufferedReader.readLine();
                System.out.println(receivedData);
            } while (isRunning);
        }
        catch (Exception e) {
            System.out.println(Thread.currentThread().getName() + " exception");
        }
    }

    @Override
    public void close() {
        isRunning = false;
    }
}
