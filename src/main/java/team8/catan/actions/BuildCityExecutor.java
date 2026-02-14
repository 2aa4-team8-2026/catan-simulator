package actions;

import board.Board;
import board.Node;
import board.StructureType;
import players.Player;

public final class BuildCityExecutor implements ActionExecutor {
    @Override
    public ActionType supportedType() {
        return ActionType.BUILD_CITY;
    }

    @Override
    public boolean execute(Board board, Player player, Action action, boolean chargeCost) {
        Node node = board.getNode(action.getTargetId());
        if (node == null) {
            return false;
        }
        if (node.getOwnerId() != player.getId()) {
            return false;
        }
        if (node.getStructureType() != StructureType.SETTLEMENT) {
            return false;
        }

        node.upgradeToCity();
        if (chargeCost) {
            player.applyActionCost(action);
        }
        player.addVictoryPoints(1);
        return true;
    }
}
