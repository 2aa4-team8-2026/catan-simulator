package rules;

import actions.Action;
import actions.ActionType;
import board.Board;
import board.Node;
import gameplay.GamePhase;
import players.Player;

public final class SettlementDistanceRuleModule implements RuleModule {
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
