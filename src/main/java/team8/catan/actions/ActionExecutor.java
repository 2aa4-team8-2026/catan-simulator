package team8.catan.actions;

import team8.catan.board.Board;
import team8.catan.players.Player;

public interface ActionExecutor {
    ActionType supportedType();

    boolean execute(Board board, Player player, Action action, boolean chargeCost);
}
