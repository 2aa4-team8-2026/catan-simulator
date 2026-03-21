package team8.catan.rules;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.Node;
import team8.catan.board.StructureType;
import team8.catan.gameplay.GamePhase;
import team8.catan.support.TestPlayer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionGenerationServiceTest {
    @Test
    void generate_includesCoreActionsAndTargets() {
        Board board = new Board(
            List.of(new Node(0), new Node(1), new Node(2)),
            List.of(new Edge(0, 0, 1), new Edge(1, 1, 2))
        );
        board.getNode(1).restoreState(0, StructureType.SETTLEMENT);
        board.getEdge(0).placeRoad(0);
        TestPlayer player = new TestPlayer(0);

        ActionGenerationService service = new ActionGenerationService();
        List<Action> actions = service.generate(board, player, GamePhase.RUNNING);

        assertTrue(actions.contains(new Action(ActionType.PASS, -1)));
        assertTrue(actions.contains(new Action(ActionType.BUILD_CITY, 1)));
        assertTrue(actions.contains(new Action(ActionType.BUILD_SETTLEMENT, 0)));
        assertTrue(actions.contains(new Action(ActionType.BUILD_SETTLEMENT, 2)));
        assertTrue(actions.contains(new Action(ActionType.BUILD_ROAD, 1)));
    }

    @Test
    void getCandidateTargets_andIsCandidateTarget_handleControlAndInvalidTargets() {
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of(new Edge(0, 0, 1)));
        board.getEdge(0).placeRoad(0);
        TestPlayer player = new TestPlayer(0);
        ActionGenerationService service = new ActionGenerationService();

        assertArrayEquals(new int[0], service.getCandidateTargets(ActionType.UNDO, board, player, GamePhase.RUNNING));
        assertTrue(service.isCandidateTarget(new Action(ActionType.PASS, -1), board, player, GamePhase.RUNNING));
        assertFalse(service.isCandidateTarget(new Action(ActionType.BUILD_ROAD, 0), board, player, GamePhase.RUNNING));
    }
}
