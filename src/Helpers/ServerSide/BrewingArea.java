package Helpers.ServerSide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrewingArea {
    public List<Pair<String, Drink>> brewingList;

    public BrewingArea() {
        this.brewingList = Collections.synchronizedList(new ArrayList<>(4));
    }
    public int[] getCountAll(String username) {
        int[] count = new int[DrinkType.values().length];
        for (Pair<String, Drink> drink : brewingList) {
            if (username == null || drink.getA().equals(username)) {
                switch (drink.getB().type) {
                    case COFFEE -> count[0]++;
                    case TEA -> count[1]++;
                }
            }
        }
        return count;
    }
    public int getCountDrink(String username, DrinkType type) {
        int count = 0;
        for (Pair<String, Drink> drink : brewingList) {
            if (username == null && drink.getB().type.equals(type)) {
                count++;
            } else if (drink.getA().equals(username) && drink.getB().type.equals(type)) {
                count++;
            }
        }
        return count;
    }

}
