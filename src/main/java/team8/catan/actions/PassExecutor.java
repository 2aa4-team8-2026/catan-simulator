package actions;

import board.Board;
import players.Player;

public final class PassExecutor implements ActionExecutor {
    @Override
    public ActionType supportedType() {
        return ActionType.PASS;
    }

    @Override
    public boolean execute(Board board, Player player, Action action, boolean chargeCost) {
        return true;
    }
}
