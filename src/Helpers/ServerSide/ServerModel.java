package Helpers.ServerSide;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerModel {
    private static final int PORT = 8888;
    public BlockingQueue<Order> orders = new LinkedBlockingQueue<>();
    ;
    public WaitingArea waitingArea;
    public BrewingArea brewingArea;
    public TrayArea trayArea;
    private static final Map<String, ClientHandler> clients = new HashMap<>();
    static ServerSocket serverSocket;
    private static final String SHARED_SECRET_KEY = "SecretKeyForAssignment";
    // Use PropertyChangeSupport to notify observers about changes
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Server model class for server side data processing and logic following an MVC design pattern
     * Handles the order processing, server startup, client listener, authentication and brewing threads
     */
    ServerModel() {
        waitingArea = new WaitingArea();
        brewingArea = new BrewingArea();
        trayArea = new TrayArea();
    }

    /**
     * Server startup makes a connection available on PORT and listens for client connections, creates threads for brewing
     */
    public void serverStartup() throws IOException {

        serverSocket = new ServerSocket(PORT);
        System.out.println("Café Server is running on port " + PORT + ". Waiting for a client connection...");
        Thread clientListener = new Thread(this::clientListener);
        clientListener.start();

        Brewery breweryCoffee = new Brewery(DrinkType.COFFEE, this, 2, 45000, propertyChangeSupport);
        Brewery breweryTea = new Brewery(DrinkType.TEA, this, 2, 30000, propertyChangeSupport);
        Thread threadCoffee = new Thread(breweryCoffee);
        threadCoffee.start();
        Thread threadTea = new Thread(breweryTea);
        threadTea.start();
    }

    /**
     * Client listener method listens for new client connections and creates a client handler thread per client
     */
    private void clientListener() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                propertyChangeSupport.firePropertyChange("clientArrival", null, clientSocket);
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                if (authenticate(clientHandler)) {
                    Thread thread = new Thread(clientHandler);
                    thread.start();
                } else {
                    clientHandler.closeConnection();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    private boolean authenticate(ClientHandler clientHandler) throws IOException {
        // Server sends a random challenge to the client
        String challenge = generateChallenge();
        clientHandler.sendMessage("Challenge: " + challenge);
        // Client responds with a hashed version of the challenge using a secret key
        String clientResponse = clientHandler.readMessage();
        System.out.println(clientResponse);

        // Server verifies the client's response
        if (verifyClientResponse(clientResponse, challenge)) {
            clientHandler.sendMessage("Authentication successful. You are now connected.");
            return true;
        } else {
            //clientHandler.sendMessage("Authentication failed. Closing connection.");
            return true;
        }
    }

    public String generateChallenge() {
        // Generate a random challenge using a secure random number generator
        SecureRandom secureRandom = new SecureRandom();
        byte[] challengeBytes = new byte[32];
        secureRandom.nextBytes(challengeBytes);

        return Base64.getEncoder().encodeToString(challengeBytes);
    }
    public boolean verifyClientResponse(String clientResponse, String challenge) {
        try {
            // Use HMAC-SHA256
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(SHARED_SECRET_KEY.getBytes(), "HmacSHA256");
            sha256Hmac.init(secretKey);

            // Calculate the HMAC of the challenge
            byte[] hmacBytes = sha256Hmac.doFinal(challenge.getBytes());
            String calculatedResponse = Base64.getEncoder().encodeToString(hmacBytes);

            // Verify the client's response
            return calculatedResponse.equals(clientResponse);
        } catch (Exception e) {
            System.out.println("Error in Barista Server: " + e.getMessage());
            return false;
        }
    }
    /**
     * process command method handles client side inputs
     * @param command input from client side
     * @param username name of client
     */
    public synchronized void processCommand(String username, String command) {
        // command processing
        // Example: order 2 teas and 3 coffees
        // Example: order status
        String[] tokens;
        String commandType;
        if (command.equals("order status")) {
            tokens = command.split(" ");
            commandType = tokens[1].toLowerCase();
        } else {
            tokens = command.split(" ");
            commandType = tokens[0].toLowerCase();
        }

        switch (commandType) {
            case "order":
                try {
                    // Parse quantities directly from the split array
                    int numFirst = Integer.parseInt(tokens[1]);
                    int numSecond;
                    try {
                        numSecond = Integer.parseInt(tokens[4]);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        numSecond = 0;
                    }
                    // Create a list to store drinks
                    List<Drink> drinks = new ArrayList<>();

                    if (tokens[2].equals("tea")){
                        for (int i = 0; i < numFirst; i++) {
                            drinks.add(new Drink(DrinkType.TEA));
                        }

                        for (int i = 0; i < numSecond; i++) {
                            drinks.add(new Drink(DrinkType.COFFEE));
                        }
                    } else {
                        for (int i = 0; i < numSecond; i++) {
                            drinks.add(new Drink(DrinkType.COFFEE));
                        }

                        for (int i = 0; i < numFirst; i++) {
                            drinks.add(new Drink(DrinkType.TEA));
                        }
                    }
                    // Create drinks based on quantities

                    // Convert the list of drinks to an array
                    Drink[] drinksArray = drinks.toArray(new Drink[0]);

                    // Create an order with the username and drinks array
                    Order order = new Order(username, drinksArray);

                    // Process the order
                    processOrder(order);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    clients.get(username).sendMessage("Invalid order format. Please use the format: order <numTeas> tea and <numCoffees> coffee");
                }
                break;
            case "status":
                //broadcasts the status of their order to the user
                broadcastOrderStatus(username);
                break;
            case "exit":
                //exit command checks to see if drinks belonging to the user can be repurposed
                //checks for items in tray area queue
                if (Arrays.stream(trayArea.getCountAll(username)).sum() != 0) {
                    for (Pair<String, Drink> first : trayArea.trayQueue) {
                        //checks if a drink with a matching name is found
                        if (first.getA().equals(username)) {
                            //distributes the found drink to another client
                            String toClient = findMatchDrinkType(first);
                            if (toClient == null) {
                                //if no other potential client found remove it from the brewing area
                                trayArea.trayQueue.remove(first);
                            } else {
                                //sets the name belonging to drink in waiting area to the name in the tray area drink
                                System.out.println("1 " + first.getB().type + " in " + username + "'s tray has been transferred to " + toClient + "'s tray");
                                first.setA(toClient);
                            }
                        }
                    }
                }

                //checks for items in brewing area list
                if (Arrays.stream(brewingArea.getCountAll(username)).sum() != 0) {
                    for (Pair<String, Drink> first : brewingArea.brewingList) {
                        //checks if a drink with a matching name is found
                        if (first.getA().equals(username)) {
                            //distributes the found drink to another client
                            String toClient = findMatchDrinkType(first);
                            if (toClient == null) {
                                //if no other potential client found remove it from the brewing area
                                brewingArea.brewingList.remove(first);
                            } else {
                                //sets the name belonging to drink in waiting area to the name in the brewing area drink
                                System.out.println("1 " + first.getB().type + " currently brewing for " + username + " has been transferred to " + toClient + "'s order");
                                first.setA(toClient);
                            }
                        }
                    }
                }
                removeClient(username);
                break;
            default:
                clients.get(username).sendMessage("Unknown command. Please try again.");
        }

    }
    /**
     * redistributes drinks to other clients on client exit command
     * @param first pair of username and drink from an area
     */
    private String findMatchDrinkType(Pair<String, Drink> first) {
        //checks waiting area queue for a drink of the same type
        for (Pair<String, Drink> second: waitingArea.waitingQueue) {
            if (first.getB().type.equals(second.getB().type)) {
                //discards the drink in the waiting area
                waitingArea.waitingQueue.remove(second);
                return second.getA();
            }
        }
        return null;
    }
    /**
     * process order method for checking duplicate orders to add onto or add new order to orders queue
     * @param order details such as name and drinks ordered
     */
    private synchronized void processOrder(Order order) {
        // Check if the client already has an order in the waiting area
        boolean appended = false;
        if (!orders.isEmpty()) {
            for (Order existingOrder : orders) {
                if (existingOrder.getClientName().equals(order.getClientName())) {
                    // Add the items to the existing order
                    existingOrder.append(order.getMap());
                    order.getMap().forEach((k, v) -> waitingArea.addDrink(order.getClientName(), k, v));
                    appended = true;
                }
            }
        }
        if (!appended) {
            order.getMap().forEach((k, v) -> waitingArea.addDrink(order.getClientName(), k, v));
            orders.add(order);
        }
        // Add the order to the waiting area
        clients.get(order.getClientName()).sendMessage("Order received for " + order.getClientName() + " (" + order + ")");
        System.out.println("Order received for " + order.getClientName() + " (" + order + ")");
        propertyChangeSupport.firePropertyChange("updateState", null, this);
    }
    public synchronized void completeOrders() {
        for (Order nextOrderInLine : orders) {
            if (nextOrderInLine != null) {
                int count = 0;
                boolean[] completed = new boolean[nextOrderInLine.getMap().size()];
                for (Map.Entry<DrinkType, Integer> entry : nextOrderInLine.getMap().entrySet()) {
                    DrinkType key = entry.getKey();
                    Integer value = entry.getValue();

                    if (trayArea.getCountDrink(nextOrderInLine.getClientName(), key) == value) {
                        completed[count] = true;
                    }
                    count++;
                }
                if (areAllValuesTrue(completed)) {
                    trayArea.removeDrinks(nextOrderInLine.getClientName());
                    clients.get(nextOrderInLine.getClientName()).sendMessage("order delivered to " + nextOrderInLine.getClientName()
                            + " (" + nextOrderInLine.getMap().get(DrinkType.TEA) + " teas and "
                            + nextOrderInLine.getMap().get(DrinkType.TEA) + " coffee)");
                    orders.remove(nextOrderInLine);
                }
            }
        }
    }
    private static boolean areAllValuesTrue(boolean[] array) {
        for (boolean value : array) {
            if (!value) {
                return false;
            }
        }
        return true;
    }
    public synchronized void addClient(String name, ClientHandler clientHandler) {
        clients.put(name, clientHandler);
        propertyChangeSupport.firePropertyChange("updateState", null, this);
    }
    public synchronized void removeClient(String name) {
        clients.remove(name);
        propertyChangeSupport.firePropertyChange("updateState", null, this);
    }
    public synchronized Map<String, ClientHandler> getClients() {
        return clients;
    }
    public synchronized void broadcastOrderStatus(String username) {
        String message = "Order status for " + username + ":\n";
        message = getString(message, waitingArea.getCountAll(username));
        message = message + " in waiting area\n";


        message = getString(message, brewingArea.getCountAll(username));
        message = message + " currently being prepared\n";

        message = getString(message, trayArea.getCountAll(username));

        message = message + " currently in the tray";
        clients.get(username).sendMessage(message);
    }

    private String getString(String message, int[] countDrink) {
        message = message + "- ";
        if (countDrink[0] > 0) {
            message = message + countDrink[0] + " coffees ";
        }
        if (countDrink[1] > 0) {
            message = message + countDrink[1] + " teas";
        }
        if (Arrays.stream(countDrink).sum() == 0) {
            message = message + "0 items";
        }
        return message;
    }

    public void closeServerSocket() throws IOException {
        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}
