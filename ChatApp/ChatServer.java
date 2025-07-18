import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345; // ✅ Don't use 3306 (reserved for MySQL)
    private static final Map<String, ClientHandler> userMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Private Chat Server started on port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket).start();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Enter your username:");
                username = in.readLine();

                synchronized (userMap) {
                    if (userMap.containsKey(username)) {
                        out.println("Username already taken.");
                        socket.close();
                        return;
                    }
                    userMap.put(username, this);
                }

                out.println("Welcome, " + username + "!");
                out.println("Commands: /users, /chat <username>, /exit");

                String targetUser = null;
                while (true) {
                    String msg = in.readLine();
                    if (msg == null) break;

                    if (msg.startsWith("/users")) {
                        synchronized (userMap) {
                            out.println("Online users: " + userMap.keySet());
                        }
                    } else if (msg.startsWith("/chat")) {
                        String[] parts = msg.split("\\s+");  // ✅ Fixed: use double backslash for regex
                        if (parts.length == 2 && userMap.containsKey(parts[1])) {
                            targetUser = parts[1];
                            out.println("Now chatting with " + targetUser);
                        } else {
                            out.println("User not found.");
                        }
                    } else if (msg.startsWith("/exit")) {
                        out.println("Goodbye!");
                        break;
                    } else if (targetUser != null) {
                        ClientHandler receiver = userMap.get(targetUser);
                        if (receiver != null) {
                            receiver.out.println(username + ": " + msg);
                            DBConnection.saveMessage(username + " -> " + targetUser, msg); // Optional
                        } else {
                            out.println("User is offline.");
                        }
                    } else {
                        out.println("Use /chat <username> to select someone to chat with.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error with user " + username);
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
                synchronized (userMap) {
                    userMap.remove(username);
                }
                System.out.println(username + " disconnected.");
            }
        }
    }
}
