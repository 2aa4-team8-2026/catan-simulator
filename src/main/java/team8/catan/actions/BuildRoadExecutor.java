package team8.catan.actions;

import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.players.Player;

public class BuildRoadExecutor implements ActionExecutor {
    @Override
    public ActionType supportedType() {
        return ActionType.BUILD_ROAD;
    }

    @Override
    public boolean execute(Board board, Player player, Action action, boolean chargeCost) {
        Edge edge = board.getEdge(action.getTargetId());
        if (edge == null || !edge.isUnowned()) {
            return false;
        }

        edge.placeRoad(player.getId());
        if (chargeCost) {
            player.applyActionCost(action);
        }
        return true;
    }
}
