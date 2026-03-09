package team8.catan.actions;

import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.players.Player;

public class BuildSettlementExecutor implements ActionExecutor {
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
