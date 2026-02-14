package players;

import board.ResourceType;
import actions.ActionType;

import java.util.HashMap;
import java.util.Map;

public class ResourceHand {
    private final Map<ResourceType, Integer> counts;

    public ResourceHand() {
        this.counts = new HashMap<>();
        for (ResourceType type : ResourceType.values()) {
            this.counts.put(type, 0);
        }
    }

    public int totalCards() {
        int total = 0;
        for (int count : counts.values()) {
            total += count;
        }
        return total;
    }

    public boolean canAfford(ActionType actionType) {
        for (Map.Entry<ResourceType, Integer> cost : actionType.getCost().entrySet()) {
            int available = counts.getOrDefault(cost.getKey(), 0);
            if (available < cost.getValue()) {
                return false;
            }
        }
        return true;
    }

    public void add(ResourceType type, int amount) {
        counts.put(type, counts.get(type) + amount);
    }

    public void spendFor(ActionType actionType) {
        for (Map.Entry<ResourceType, Integer> cost : actionType.getCost().entrySet()) {
            counts.put(cost.getKey(), counts.get(cost.getKey()) - cost.getValue());
        }
    }
}
