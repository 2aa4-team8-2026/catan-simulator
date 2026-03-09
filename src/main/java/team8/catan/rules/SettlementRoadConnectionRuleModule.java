package team8.catan.rules;

import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;

public class SettlementRoadConnectionRuleModule implements RuleModule {
    @Override
    public boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
        if (action.getActionType() != ActionType.BUILD_SETTLEMENT) {
            return true;
        }

        if (phase != GamePhase.RUNNING) {
            return true;
        }

        return board.hasIncidentRoadOwnedBy(action.getTargetId(), player.getId());
    }
}
