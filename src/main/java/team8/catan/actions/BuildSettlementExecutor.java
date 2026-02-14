package actions;

import board.Board;
import board.Node;
import players.Player;

public final class BuildSettlementExecutor implements ActionExecutor {
    @Override
    public ActionType supportedType() {
        return ActionType.BUILD_SETTLEMENT;
    }

    @Override
    public boolean execute(Board board, Player player, Action action, boolean chargeCost) {
        Node node = board.getNode(action.getTargetId());
        if (node == null || !node.isUnowned() || node.getStructureType() != null) {
            return false;
        }

        node.placeSettlement(player.getId());
        if (chargeCost) {
            player.applyActionCost(action);
        }
        player.addVictoryPoints(1);
        return true;
    }
}
