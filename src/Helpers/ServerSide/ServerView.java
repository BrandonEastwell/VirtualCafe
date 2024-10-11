package Helpers.ServerSide;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ServerView implements PropertyChangeListener {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    ServerController serverController;
    public ServerView(ServerController serverController) {
        this.serverController = serverController;
    }

    public void logStateUpdates(ServerModel model) {
        // Log status updates to the console
        System.out.println("Number of clients in the café: " + model.getClients().size());
        System.out.println("Number of clients waiting for orders: " + model.orders.size());
        System.out.println("Items in waiting area: " + model.waitingArea.getCountDrink(null, DrinkType.TEA) + " tea " + model.waitingArea.getCountDrink(null, DrinkType.COFFEE) + " coffee");
        System.out.println("Items in brewing area: " + model.brewingArea.getCountDrink(null, DrinkType.TEA) + " tea " + model.brewingArea.getCountDrink(null, DrinkType.COFFEE) + " coffee");
        System.out.println("Items in tray area: " + model.trayArea.getCountDrink(null, DrinkType.TEA) + " tea " + model.trayArea.getCountDrink(null, DrinkType.COFFEE) + " coffee");

        // Log status updates to the JSON file using Gson
        try (FileWriter writer = new FileWriter("barista_logs.json", true)) {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            logEntry.put("clients_in_cafe", model.getClients().size());
            logEntry.put("clients_waiting_for_orders", model.orders.size());
            logEntry.put("items_in_waiting_area", model.waitingArea.getCountAll(null));
            logEntry.put("items_in_brewing_area", model.brewingArea.getCountAll(null));
            logEntry.put("items_in_tray_area", model.trayArea.getCountAll(null));

            // Use Gson to convert the log entry to JSON format
            String jsonLogEntry = gson.toJson(logEntry);

            // Write log entry to the JSON file
            writer.write(jsonLogEntry + System.lineSeparator());
        } catch (IOException e) {
            // System.err.println("Error writing to log file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if ("clientArrival".equals(evt.getPropertyName())) {
            System.out.println("New client connected: " + ((Socket) evt.getNewValue()).getInetAddress().getHostAddress());
        }
    }
}
