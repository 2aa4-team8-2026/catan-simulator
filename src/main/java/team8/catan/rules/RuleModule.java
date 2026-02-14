package rules;

import actions.Action;
import board.Board;
import gameplay.GamePhase;
import players.Player;

import java.util.List;

public interface RuleModule {
    default boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
        return true;
    }

    default void onDiceRolled(int diceRoll, Board board, List<? extends Player> players, GamePhase phase) {
    }
}
