package team8.catan.board;

import org.junit.jupiter.api.Test;

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
    void Board_isRoadConnectedToPlayerNetworkReturnsFalseWhenBlockedByOpponentStructure() {
        Board board = new Board(
            List.of(new Node(0), new Node(1), new Node(2)),
            List.of(new Edge(0, 0, 1), new Edge(1, 1, 2))
        );
        board.getEdge(0).placeRoad(1);
        board.getNode(1).placeSettlement(2);

        assertFalse(board.isRoadConnectedToPlayerNetwork(1, 1));
    }
}
