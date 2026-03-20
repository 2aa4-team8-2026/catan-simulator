package team8.catan.gameplay.commands;

import team8.catan.actions.Action;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.ResourceType;
import team8.catan.logging.RoadPlacement;
import team8.catan.players.Player;

import java.util.Map;

public class BuildRoadCommand extends AbstractBuildCommand {
    private int previousRoadOwnerId;
    private Map<ResourceType, Integer> previousResources;
    private RoadPlacement appendedPlacement;

    public BuildRoadCommand(
        Board board,
        Player player,
        Action action,
        boolean chargeCost,
        java.util.List<RoadPlacement> roadPlacementOrder
    ) {
        super(board, player, action, chargeCost, roadPlacementOrder);
    }

    @Override
    protected boolean canApplyInitially() {
        Edge edge = board.getEdge(action.getTargetId());
        return edge != null && edge.isUnowned();
    }

    @Override
    protected void captureInitialState() {
        Edge edge = board.getEdge(action.getTargetId());
        previousRoadOwnerId = edge.getRoadOwnerId();
        previousResources = player.getResourceSnapshot();
    }

    @Override
    protected void applyForward() {
        Edge edge = board.getEdge(action.getTargetId());
        edge.placeRoad(player.getId());
        if (chargeCost) {
            player.applyActionCost(action);
        }
        appendedPlacement = new RoadPlacement(action.getTargetId(), player.getId());
        roadPlacementOrder.add(appendedPlacement);
    }

    @Override
    protected void restoreInitialState() {
        Edge edge = board.getEdge(action.getTargetId());
        edge.restoreRoadOwner(previousRoadOwnerId);
        player.restoreResourceSnapshot(previousResources);
        if (appendedPlacement != null) {
            roadPlacementOrder.remove(appendedPlacement);
        }
    }
}
