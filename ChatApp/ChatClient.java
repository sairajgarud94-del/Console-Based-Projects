
import java.io.*;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) {
        final String SERVER = "localhost";
        final int PORT = 12345;

        try (Socket socket = new Socket(SERVER, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.print(in.readLine() + " ");
            String username = console.readLine();
            out.println(username);

            new Thread(() -> {
                String msg;
                try {
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }).start();

            String input;
            while ((input = console.readLine()) != null) {
                out.println(input);
                if (input.equalsIgnoreCase("/exit")) break;
            }

        } catch (IOException e) {
            System.out.println("Could not connect to server.");
        }
    }
}
