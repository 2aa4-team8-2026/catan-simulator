package team8.catan.board;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardTest {
    @Test
    void Board_hasAdjacentStructureReturnsTrueForNeighborSettlement() {
        Board board = new Board(
            List.of(new Node(0), new Node(1), new Node(2)),
            List.of(new Edge(0, 0, 1), new Edge(1, 1, 2))
        );
        board.getNode(1).placeSettlement(2);

        assertTrue(board.hasAdjacentStructure(0));
        assertFalse(board.hasAdjacentStructure(1));
        assertNull(board.getNode(-1));
        assertNull(board.getEdge(5));
        assertEquals(List.of(0, 1), board.getIncidentEdgeIds(1));
        assertEquals(List.of(0, 2), board.getAdjacentNodeIds(1));
        List<Node> nodesCopy = board.getNodes();
        nodesCopy.clear();
        assertEquals(3, board.getNodes().size());
        board.getNode(0).placeSettlement(1);
        assertArrayEquals(new int[] { 2 }, board.getValidSettlementTargets(1));
        assertArrayEquals(new int[] { 0 }, board.getValidCityTargets(1));
    }

    @Test
    void Board_isRoadConnectedToPlayerNetworkReturnsFalseWhenBlockedByOpponentStructure() {
        Board board = new Board(
            List.of(new Node(0), new Node(1), new Node(2), new Node(3)),
            List.of(new Edge(0, 0, 1), new Edge(1, 1, 2), new Edge(2, 2, 3))
        );
        board.getNode(0).placeSettlement(1);
        assertTrue(board.isRoadConnectedToPlayerNetwork(0, 1));
        board.getEdge(0).placeRoad(1);
        assertTrue(board.hasIncidentRoadOwnedBy(1, 1));
        assertTrue(board.isRoadConnectedToPlayerNetwork(1, 1));
        assertArrayEquals(new int[] { 1 }, board.getValidRoadTargets(1));
        board.getNode(1).placeSettlement(2);

        assertFalse(board.isRoadConnectedToPlayerNetwork(1, 1));
        assertFalse(board.isRoadConnectedToPlayerNetwork(99, 1));
        assertArrayEquals(new int[] { }, board.getValidRoadTargets(1));
    }
}
