package team8.catan.players.strategy;

import team8.catan.actions.Action;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;

import java.util.List;

public interface ActionValuationStrategy {
    double score(Action action, Player player, GamePhase phase);

    Action chooseBest(List<Action> legalActions, Player player, GamePhase phase);
}
