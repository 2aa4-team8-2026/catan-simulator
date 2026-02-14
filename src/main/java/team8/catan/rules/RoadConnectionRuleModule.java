package team8.catan.rules;

import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;

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
