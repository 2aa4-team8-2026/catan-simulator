package team8.catan.board;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.PlayerColor;
import team8.catan.players.RandomAgent;
import team8.catan.rules.RoadConnectionRuleModule;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardTest {
    @Test
    void Board_hasAdjacentStructureReturnsTrueForNeighborSettlement() {
        Board board = new Board(
            List.of(new Node(0), new Node(1)),
            List.of(new Edge(0, 0, 1))
        );
        board.getNode(1).placeSettlement(2);

        assertTrue(board.hasAdjacentStructure(0));
    }

    @Test
    void RoadConnectionRuleModule_returnsFalseWhenBlockedByOpponentStructure() {
        Board board = new Board(
            List.of(new Node(0), new Node(1), new Node(2)),
            List.of(new Edge(0, 0, 1), new Edge(1, 1, 2))
        );
        board.getEdge(0).placeRoad(1);
        board.getNode(1).placeSettlement(2);
        RandomAgent player = new RandomAgent(1, PlayerColor.RED);

        assertFalse(new RoadConnectionRuleModule().isLegal(
            new Action(ActionType.BUILD_ROAD, 1),
            board,
            player,
            GamePhase.RUNNING
        ));
    }
}
