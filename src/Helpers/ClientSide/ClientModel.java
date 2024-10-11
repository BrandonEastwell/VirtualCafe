package Helpers.ClientSide;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

public class ClientModel {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8888;
    private ClientView clientView;
    private PrintWriter serverOutput;
    private BufferedReader serverInput;
    private Socket clientSocket;
    private String username;
    private boolean nextCommandUsername = false;
    private static final String SHARED_SECRET_KEY = "SecretKeyForAssignment";
    private Thread serverListener;

    public ClientModel() {
    }
    public void addObserver(ClientView clientView) {
        this.clientView = clientView;
    }
    public void connectToServer() throws IOException {
        username = null;
        clientSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        serverOutput = new PrintWriter(clientSocket.getOutputStream(), true);

        // Receive and print the challenge from the server
        String challenge = serverInput.readLine();
        System.out.println("Challenge received from server: " + challenge);

        // Hash the challenge using HMAC-SHA256 with the shared secret key
        String hashedResponse = hash(challenge);
        System.out.println("Hashed response: " + hashedResponse);

        // Send the hashed response back to the server
        serverOutput.println(hashedResponse);

        serverListener = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listenForMessage();
                } catch (IOException e) {
                    System.out.println("Unable to listen to server");;
                }
            }
        });
        serverListener.start();
        writeMessageToServer();
    }

    private void writeMessageToServer() {
        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            String command;
            while ((command = consoleReader.readLine()) != null) {
                if (nextCommandUsername) {
                    username = command;
                    nextCommandUsername = false;
                }
                sendMessage(command);
                if (command.equalsIgnoreCase("exit")) {
                    disconnectFromServer();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForMessage() throws IOException {
        System.out.println("Connected to the Virtual Café!");
        String serverResponse;
        while ((serverResponse = serverInput.readLine()) != null) {
            System.out.println(serverResponse);
        }
    }
    private static String hash(String challenge) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(SHARED_SECRET_KEY.getBytes(), "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] hmacBytes = sha256Hmac.doFinal(challenge.getBytes());
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void sendMessage(String command) {
        serverOutput.println(command);
    }
    private void processResponse(String command, String response) {
        if (response.equals("Cafe > Enter your name:")) {
            nextCommandUsername = true;
        }
    }
    public String readMessage() throws IOException {
        return serverInput.readLine();
    }
    public synchronized void disconnectFromServer() throws IOException {
        if (clientSocket.isConnected()) {
            clientSocket.close();
        }
        if (serverOutput != null) {
            serverOutput.close();
        }
        if (serverInput != null) {
            serverInput.close();
        }
    }

}
