package Helpers.ServerSide;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrayArea {
    public BlockingQueue<Pair<String, Drink>> trayQueue = new LinkedBlockingQueue<>();;   // Completed orders
    public TrayArea() {
    }
    public int[] getCountAll(String username) {
        int[] count = new int[DrinkType.values().length];
        for (Pair<String, Drink> drink : trayQueue) {
            if (username == null || drink.getA().equals(username)) {
                switch (drink.getB().type) {
                    case TEA -> count[0]++;
                    case COFFEE -> count[1]++;
                }
            }
        }
        return count;
    }

    public int getCountDrink(String username, DrinkType type) {
        int count = 0;
        for (Pair<String, Drink> drink : trayQueue) {
            if (username == null && drink.getB().type.equals(type)) {
                count++;
            } else if (drink.getA().equals(username) && drink.getB().type.equals(type)) {
                count++;
            }
        }
        return count;
    }

    public void removeDrinks(String username) {
        for (Pair<String, Drink> drink : trayQueue) {
            if (drink.getA().equals(username)) {
                trayQueue.remove(drink);
            }
        }
    }
}
