package rules;

import actions.Action;
import actions.ActionType;
import board.Board;
import gameplay.GamePhase;
import players.Player;

public final class SettlementRoadConnectionRuleModule implements RuleModule {
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
