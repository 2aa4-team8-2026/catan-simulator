package team8.catan.actions;

import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.board.StructureType;
import team8.catan.players.Player;

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
