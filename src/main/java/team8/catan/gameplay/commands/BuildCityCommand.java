package team8.catan.gameplay.commands;

import team8.catan.actions.Action;
import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.board.StructureType;
import team8.catan.logging.RoadPlacement;
import team8.catan.players.Player;

import java.util.List;
import java.util.Map;

public class BuildCityCommand extends AbstractBuildCommand {
    private int previousOwnerId;
    private StructureType previousStructureType;
    private Map<ResourceType, Integer> previousResources;
    private int previousVictoryPoints;

    public BuildCityCommand(
        Board board,
        Player player,
        Action action,
        boolean chargeCost,
        List<RoadPlacement> roadPlacementOrder
    ) {
        super(board, player, action, chargeCost, roadPlacementOrder);
    }

    @Override
    protected boolean canApplyInitially() {
        Node node = board.getNode(action.getTargetId());
        return node != null
            && node.getOwnerId() == player.getId()
            && node.getStructureType() == StructureType.SETTLEMENT;
    }

    @Override
    protected void captureInitialState() {
        Node node = board.getNode(action.getTargetId());
        previousOwnerId = node.getOwnerId();
        previousStructureType = node.getStructureType();
        previousResources = player.getResourceSnapshot();
        previousVictoryPoints = player.getVictoryPoints();
    }

    @Override
    protected void applyForward() {
        Node node = board.getNode(action.getTargetId());
        node.upgradeToCity();
        if (chargeCost) {
            player.applyActionCost(action);
        }
        player.addVictoryPoints(1);
    }

    @Override
    protected void restoreInitialState() {
        Node node = board.getNode(action.getTargetId());
        node.restoreState(previousOwnerId, previousStructureType);
        player.restoreResourceSnapshot(previousResources);
        player.restoreVictoryPoints(previousVictoryPoints);
    }
}
