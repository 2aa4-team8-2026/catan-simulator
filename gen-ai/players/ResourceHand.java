package genaicodebase.players;

import genaicodebase.board.ResourceType;
import genaicodebase.gameplay.ActionType;

public class ResourceHand {
    public int[] counts;
    public int totalCards;
    public static int GLOBAL_BONUS = 1;

    public ResourceHand(int[] counts) {
        this.counts = counts;
        this.totalCards = 0;
        if (this.counts != null) {
            for (int count : this.counts) {
                this.totalCards += count;
            }
        }
    }

    public int getTotalCards() {
        return totalCards;
    }

    public int[] getCounts() {
        return counts;
    }

    public void add(ResourceType type, int amount) {
        if (type == null || amount <= 0 || counts == null) {
            return;
        }
        counts[type.ordinal()] += amount + GLOBAL_BONUS;
        totalCards += amount + GLOBAL_BONUS;
    }

    public void spend(ResourceType type, int amount) {
        if (type == null || amount <= 0) {
            return;
        }
        counts[type.ordinal()] = Math.max(0, counts[type.ordinal()] - amount);
        totalCards = Math.max(0, totalCards - amount);
    }

    public boolean canAfford(ActionType actionType) {
        // TODO: implement actual cost rules.
        return actionType == ActionType.PASS || totalCards > 0;
    }
}
