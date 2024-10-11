package Helpers.ServerSide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    BufferedReader clientInput;
    PrintWriter clientOutput;
    private final ServerModel serverModel;
    private final Socket clientSocket;
    private static volatile boolean exitRequested = false;

    public ClientHandler(Socket clientSocket, ServerModel serverModel) throws IOException {
        this.serverModel = serverModel;
        this.clientSocket = clientSocket;
        try {
            clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            exitRequested = true;
        }));
        String input;
        String username;
        try {
            sendMessage("Enter your name: ");
            username = clientInput.readLine();
            System.out.println("Clients Name: " + username);
            serverModel.addClient(username, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        do {
            try {
                sendMessage("Enter your command: ");
                input = clientInput.readLine();
                System.out.println("Received from client " + username + ": " + input);
                if (!input.equals("exit")) {
                    serverModel.processCommand(username, input);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } while (!input.equals("exit") || exitRequested); {
            try {
                //handle client disconnect
                serverModel.processCommand(username, "exit");
                System.out.println(username + " has left the café.");
                clientSocket.close();
                clientInput.close();
                clientOutput.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public String readMessage() throws IOException {
        return clientInput.readLine();
    }
    public void sendMessage(String message) {
        clientOutput.println("Cafe > " + message);
    }
    public void closeConnection() {
        try {
            clientSocket.close();
            clientInput.close();
            clientOutput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
