package team8.catan.rules;

import team8.catan.actions.Action;
import team8.catan.actions.ActionFeature;
import team8.catan.actions.ActionTarget;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.Node;
import team8.catan.board.StructureType;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;

import java.util.ArrayList;
import java.util.List;

public class ActionGenerationService {
    private final RoadNetworkService roadNetworkService;

    public ActionGenerationService() {
        this(new RoadNetworkService());
    }

    ActionGenerationService(RoadNetworkService roadNetworkService) {
        this.roadNetworkService = roadNetworkService;
    }

    public List<Action> generate(Board board, Player player, GamePhase phase) {
        List<Action> actions = new ArrayList<>();
        for (ActionType actionType : ActionType.values()) {
            if (actionType.getFeature() != ActionFeature.CORE) {
                continue;
            }
            for (int targetId : getCandidateTargets(actionType, board, player, phase)) {
                actions.add(new Action(actionType, targetId));
            }
        }
        return actions;
    }

    public int[] getCandidateTargets(ActionType actionType, Board board, Player player, GamePhase phase) {
        return switch (actionType) {
            case PASS -> new int[] { ActionTarget.NO_TARGET_ID };
            case BUILD_SETTLEMENT -> settlementTargets(board);
            case BUILD_CITY -> cityTargets(board, player);
            case BUILD_ROAD -> roadTargets(board, player);
            case UNDO, REDO -> new int[0];
        };
    }

    public boolean isCandidateTarget(Action action, Board board, Player player, GamePhase phase) {
        int targetId = action.getTargetId();
        for (int validTarget : getCandidateTargets(action.getActionType(), board, player, phase)) {
            if (validTarget == targetId) {
                return true;
            }
        }
        return false;
    }

    private int[] settlementTargets(Board board) {
        List<Integer> valid = new ArrayList<>();
        for (Node node : board.getNodes()) {
            if (node.isUnowned() && node.getStructureType() == null) {
                valid.add(node.getId());
            }
        }
        return toIntArray(valid);
    }

    private int[] cityTargets(Board board, Player player) {
        List<Integer> valid = new ArrayList<>();
        for (Node node : board.getNodes()) {
            if (node.getOwnerId() == player.getId() && node.getStructureType() == StructureType.SETTLEMENT) {
                valid.add(node.getId());
            }
        }
        return toIntArray(valid);
    }

    private int[] roadTargets(Board board, Player player) {
        List<Integer> valid = new ArrayList<>();
        for (Edge edge : board.getEdges()) {
            if (edge.isUnowned() && roadNetworkService.isRoadConnectedToPlayerNetwork(board, edge.getId(), player.getId())) {
                valid.add(edge.getId());
            }
        }
        return toIntArray(valid);
    }

    private int[] toIntArray(List<Integer> values) {
        int[] out = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i);
        }
        return out;
    }
}
