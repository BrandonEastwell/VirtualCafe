package Helpers.ServerSide;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WaitingArea {
    public BlockingQueue<Pair<String, Drink>> waitingQueue = new LinkedBlockingQueue<>(); // Orders waiting to be processed

    public WaitingArea() {
    }
    public int[] getCountAll(String username) {
        int[] count = new int[DrinkType.values().length];
        for (Pair<String, Drink> drink : waitingQueue) {
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
        for (Pair<String, Drink> drink : waitingQueue) {
            if (username == null && drink.getB().type.equals(type)) {
                count++;
            } else if (drink.getA().equals(username) && drink.getB().type.equals(type)) {
                count++;
            }
        }
        return count;
    }
    public void addDrink(String username, DrinkType k, Integer v) {
        for (int i = 0; i < v; i++) {
            waitingQueue.add(new Pair<>(username, new Drink(k)));
        }
    }
}
