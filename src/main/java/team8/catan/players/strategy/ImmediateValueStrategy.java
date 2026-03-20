package team8.catan.players.strategy;

import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.ResourceType;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ImmediateValueStrategy implements ActionValuationStrategy {
    private static final double VICTORY_POINT_VALUE = 1.0;
    private static final double BUILD_WITHOUT_VP_VALUE = 0.8;
    private static final double LOW_HAND_VALUE = 0.5;

    private final Random random;

    public ImmediateValueStrategy(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public double score(Action action, Player player, GamePhase phase) {
        double total = baseValue(action.getActionType());
        if (spendsCardsToBelowFive(action, player)) {
            total += LOW_HAND_VALUE;
        }
        return total;
    }

    @Override
    public Action chooseBest(List<Action> legalActions, Player player, GamePhase phase) {
        List<Action> bestActions = new ArrayList<>();
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Action action : legalActions) {
            double score = score(action, player, phase);
            if (score > bestScore) {
                bestScore = score;
                bestActions.clear();
                bestActions.add(action);
            } else if (Double.compare(score, bestScore) == 0) {
                bestActions.add(action);
            }
        }

        return bestActions.get(random.nextInt(bestActions.size()));
    }

    private static double baseValue(ActionType actionType) {
        return switch (actionType) {
            case BUILD_SETTLEMENT, BUILD_CITY -> VICTORY_POINT_VALUE;
            case BUILD_ROAD -> BUILD_WITHOUT_VP_VALUE;
            case PASS, UNDO, REDO -> 0.0;
        };
    }

    private static boolean spendsCardsToBelowFive(Action action, Player player) {
        Map<ResourceType, Integer> cost = action.getActionType().getCost();
        if (cost.isEmpty()) {
            return false;
        }

        Map<ResourceType, Integer> snapshot = player.getResourceSnapshot();
        int remaining = 0;
        for (ResourceType type : ResourceType.values()) {
            int current = snapshot.getOrDefault(type, 0);
            int spent = cost.getOrDefault(type, 0);
            remaining += current - spent;
        }
        return remaining < 5;
    }
}
