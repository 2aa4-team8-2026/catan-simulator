package team8.catan.rules;

import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;

public class SettlementDistanceRuleModule implements RuleModule {
    @Override
    public boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
        if (action.getActionType() != ActionType.BUILD_SETTLEMENT) {
            return true;
        }

        int nodeId = action.getTargetId();
        Node node = board.getNode(nodeId);
        if (node == null || !node.isUnowned() || node.getStructureType() != null) {
            return false;
        }

        return !board.hasAdjacentStructure(nodeId);
    }
}
