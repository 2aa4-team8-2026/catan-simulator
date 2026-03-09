package team8.catan.players;

import team8.catan.board.ResourceType;
import team8.catan.actions.ActionType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

final class ResourceHand {
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

    public int getCount(ResourceType type) {
        return counts.getOrDefault(type, 0);
    }

    public Map<ResourceType, Integer> snapshot() {
        return new LinkedHashMap<>(counts);
    }

    public int discardRandomCards(int count, Random random) {
        if (count <= 0) {
            return 0;
        }

        int discarded = 0;
        for (int i = 0; i < count; i++) {
            ResourceType removed = removeRandomCard(random);
            if (removed == null) {
                break;
            }
            discarded++;
        }
        return discarded;
    }

    public ResourceType removeRandomCard(Random random) {
        int total = totalCards();
        if (total <= 0) {
            return null;
        }

        int pick = random.nextInt(total);
        int cumulative = 0;
        for (ResourceType type : ResourceType.values()) {
            int available = counts.getOrDefault(type, 0);
            cumulative += available;
            if (pick < cumulative) {
                counts.put(type, available - 1);
                return type;
            }
        }

        return null;
    }

    public void spendFor(ActionType actionType) {
        for (Map.Entry<ResourceType, Integer> cost : actionType.getCost().entrySet()) {
            counts.put(cost.getKey(), counts.get(cost.getKey()) - cost.getValue());
        }
    }
}
