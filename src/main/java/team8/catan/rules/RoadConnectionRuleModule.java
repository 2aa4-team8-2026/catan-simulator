package rules;

import actions.Action;
import actions.ActionType;
import board.Board;
import board.Edge;
import gameplay.GamePhase;
import players.Player;

public final class RoadConnectionRuleModule implements RuleModule {
    @Override
    public boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
        if (action.getActionType() != ActionType.BUILD_ROAD) {
            return true;
        }

        Edge edge = board.getEdge(action.getTargetId());
        if (edge == null || !edge.isUnowned()) {
            return false;
        }

        return board.isRoadConnectedToPlayerNetwork(edge.getId(), player.getId());
    }
}
