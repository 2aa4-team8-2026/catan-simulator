package team8.catan.rules;

import team8.catan.actions.Action;
import team8.catan.board.Board;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;

import java.util.List;

public interface RuleModule {
    default boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
        return true;
    }

    default void onDiceRolled(
        int diceRoll,
        Player roller,
        Board board,
        List<? extends Player> players,
        GamePhase phase
    ) {
    }
}
