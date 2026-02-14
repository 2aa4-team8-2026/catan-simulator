package actions;

import board.Board;
import players.Player;

public interface ActionExecutor {
    ActionType supportedType();

    boolean execute(Board board, Player player, Action action, boolean chargeCost);
}
