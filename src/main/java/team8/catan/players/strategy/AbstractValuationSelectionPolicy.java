package team8.catan.players.strategy;

import team8.catan.actions.Action;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractValuationSelectionPolicy extends AbstractActionSelectionPolicy {
    @Override
    protected final Action selectFromLegalActions(List<Action> legalActions, Player player, GamePhase phase) {
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

        return breakTie(bestActions);
    }

    protected abstract double score(Action action, Player player, GamePhase phase);

    protected Action breakTie(List<Action> bestActions) {
        return bestActions.get(0);
    }
}
