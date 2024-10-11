package Helpers.ServerSide;

import java.util.HashMap;
import java.util.Map;

public class Order {
    private final String username;
    private Map<DrinkType, Integer> drinks = new HashMap<>();

    public Order(String username, Drink[] drinks) {
        for (Drink drink: drinks) {
            //Check if the key is present
            if (this.drinks.containsKey(drink.type)) {
                //Retrieve the current value
                int currentValue = this.drinks.get(drink.type);

                //Increment the value
                int newValue = currentValue + 1;

                //Put the updated value back into the HashMap
                this.drinks.put(drink.type, newValue);
            } else {
                this.drinks.put(drink.type, 1);

            }
        }
        this.username = username;

    }
    public void append(Map<DrinkType, Integer> map) {
        map.forEach((k, v) -> drinks.merge(k, v, Integer::sum));
    }
    public Map<DrinkType, Integer> getMap() {
        return drinks;
    }
    @Override
    public String toString() {
        return drinks.get(DrinkType.TEA) + " teas and " + drinks.get(DrinkType.COFFEE) + " coffees";
    }
    public String getClientName() {
        return username;
    }
}
