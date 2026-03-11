package team8.catan.actions;

import team8.catan.board.Board;
import team8.catan.players.Player;

public class PassExecutor implements ActionExecutor {
    @Override
    public ActionType supportedType() {
        return ActionType.PASS;
    }

    @Override
    public boolean execute(Board board, Player player, Action action, boolean chargeCost) {
        return true;
    }
}
