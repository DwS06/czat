import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        System.out.println("Serwer uruchomiony na porcie " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients.values()) {
                client.sendMessage(message);
            }
        }
    }

    static void sendPrivate(String from, String to, String message) {
        ClientHandler receiver = clients.get(to);
        if (receiver != null) {
            receiver.sendMessage("[Priv od " + from + "]: " + message);
        }
    }

    static void updateUserLists() {
        StringBuilder userList = new StringBuilder("/users ");
        for (String name : clients.keySet()) {
            userList.append(name).append(",");
        }
        String list = userList.toString();
        for (ClientHandler client : clients.values()) {
            client.sendMessage(list);
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // pobierz nazwę użytkownika
                out.println("Podaj login:");
                username = in.readLine();
                clients.put(username, this);
                broadcast("*** " + username + " dołączył do czatu ***");
                updateUserLists();

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/w ")) {
                        // /w nazwa wiadomość
                        String[] parts = message.split(" ", 3);
                        if (parts.length >= 3) {
                            sendPrivate(username, parts[1], parts[2]);
                        }
                    } else {
                        broadcast(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println(username + " opuścił czat.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {}
                clients.remove(username);
                broadcast("*** " + username + " wyszedł z czatu ***");
                updateUserLists();
            }
        }

        void sendMessage(String msg) {
            out.println(msg);
        }
    }
}
