package rules;

import actions.Action;
import actions.ActionType;
import board.Board;
import board.Node;
import board.StructureType;
import gameplay.GamePhase;
import players.Player;

public final class CityRequiresSettlementRuleModule implements RuleModule {
    @Override
    public boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
        if (action.getActionType() != ActionType.BUILD_CITY) {
            return true;
        }

        Node node = board.getNode(action.getTargetId());
        if (node == null) {
            return false;
        }

        return node.getOwnerId() == player.getId()
            && node.getStructureType() == StructureType.SETTLEMENT;
    }
}
