package Helpers.ServerSide;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class Brewery implements Runnable {
    private DrinkType type;
    private ServerModel serverModel;
    private int MAX_BREWING_CAPACITY;
    private int BREW_TIME_IN_MILLISECONDS;
    PropertyChangeSupport propertyChangeSupport;
    public Brewery(DrinkType drinkType, ServerModel serverModel, int MAX_BREWING_CAPACITY, int BREW_TIME_IN_MILLISECONDS, PropertyChangeSupport pcs) {
        this.serverModel = serverModel;
        this.type = drinkType;
        this.MAX_BREWING_CAPACITY = MAX_BREWING_CAPACITY;
        this.BREW_TIME_IN_MILLISECONDS = BREW_TIME_IN_MILLISECONDS;
        this.propertyChangeSupport = pcs;
    }

    @Override
    public void run() {
        //checks if brewing area is at max brewing capacity
        //if not, it moves the order to the brewing area from waiting area
        List<Pair<String, Drink>> brewing = new ArrayList<>(MAX_BREWING_CAPACITY);
        try {
            while (!Thread.interrupted()) {
                if (serverModel.brewingArea.getCountDrink(null, type) < MAX_BREWING_CAPACITY && !serverModel.waitingArea.waitingQueue.isEmpty()) {
                    for (Pair<String, Drink> drink : serverModel.waitingArea.waitingQueue) {
                        if (drink.getB().type.equals(type)) {
                            serverModel.brewingArea.brewingList.add(drink);
                            brewing.add(drink);
                            serverModel.waitingArea.waitingQueue.remove(drink);
                        }
                        if (serverModel.brewingArea.getCountDrink(null, type) == MAX_BREWING_CAPACITY) {
                            break;
                        }
                    }
                    propertyChangeSupport.firePropertyChange("updateState", null, serverModel);
                    Thread.sleep(BREW_TIME_IN_MILLISECONDS);

                    serverModel.trayArea.trayQueue.addAll(brewing);
                    serverModel.brewingArea.brewingList.removeAll(brewing);
                    serverModel.completeOrders();
                    propertyChangeSupport.firePropertyChange("updateState", null, serverModel);
                    brewing.clear();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
