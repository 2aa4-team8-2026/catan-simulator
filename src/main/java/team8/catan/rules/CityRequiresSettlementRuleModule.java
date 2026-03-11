package team8.catan.rules;

import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.board.StructureType;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;

public class CityRequiresSettlementRuleModule implements RuleModule {
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
