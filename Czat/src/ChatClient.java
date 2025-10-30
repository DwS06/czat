import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UserInterface ui;
    private String username;

    public ChatClient(String host, int port, String username, UserInterface ui) throws IOException {
        this.ui = ui;
        this.username = username;
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // odbiór wiadomości
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.startsWith("/users ")) {
                        String users = msg.substring(7);
                        ui.updateUsers(users);
                    } else {
                        ui.appendMessage(msg);
                    }
                }
            } catch (IOException e) {
                ui.appendMessage("Rozłączono z serwerem.");
            }
        }).start();

        // wysyłanie loginu
        in.readLine(); // oczekuje "Podaj login:"
        out.println(username);
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {}
    }
}
