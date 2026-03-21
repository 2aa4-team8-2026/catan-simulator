package team8.catan.board;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.PlayerColor;
import team8.catan.players.RandomAgent;
import team8.catan.rules.RoadConnectionRuleModule;

import java.util.Random;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void Board_handlesInvalidIdsRobberPlacementAndRandomTileSelection() {
        Tile tile = new Tile(0, 0, 0, 0, ResourceType.BRICK, 5, new int[] {0, 1});
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of(new Edge(0, 0, 1)), List.of(tile), 0);

        assertNull(board.getNode(-1));
        assertNull(board.getNode(2));
        assertNull(board.getEdge(-1));
        assertNull(board.getEdge(2));
        assertNull(board.getTile(-1));
        assertNull(board.getTile(2));
        assertEquals(0, board.getRobberTileId());
        assertEquals(0, board.getRandomTileId(new Random(0)));

        board.restoreRobberTileId(-1);
        assertEquals(-1, board.getRobberTileId());

        board.placeRobber(0);
        assertEquals(0, board.getRobberTileId());
        assertThrows(IllegalArgumentException.class, () -> board.placeRobber(1));

        Board emptyTileBoard = new Board(List.of(new Node(0)), List.of());
        assertEquals(-1, emptyTileBoard.getRandomTileId(new Random(0)));
    }

    @Test
    void Board_queriesAdjacencyTilesAndIncidentRoadOwnership() {
        Node node0 = new Node(0);
        Node node1 = new Node(1);
        Node node2 = new Node(2);
        Node node3 = new Node(3);
        node1.placeSettlement(7);
        Edge edge0 = new Edge(0, 0, 1);
        Edge edge1 = new Edge(1, 1, 2);
        edge1.placeRoad(7);
        Tile tile0 = new Tile(0, 0, 0, 0, ResourceType.BRICK, 4, new int[] {0, 1, 3});
        Tile tile1 = new Tile(1, 1, -1, 0, ResourceType.LUMBER, 8, new int[] {2, 3});
        Board board = new Board(List.of(node0, node1, node2, node3), List.of(edge0, edge1), List.of(tile0, tile1), 0);

        assertEquals(0, board.getEdgeIdBetweenNodes(0, 1));
        assertEquals(0, board.getEdgeIdBetweenNodes(1, 0));
        assertEquals(-1, board.getEdgeIdBetweenNodes(0, 3));
        assertArrayEquals(new int[] {0, 1, 3}, board.getAdjacentNodeIdsForTile(0));
        assertArrayEquals(new int[0], board.getAdjacentNodeIdsForTile(99));
        assertTrue(board.hasStructureAdjacentToTile(0, 7));
        assertFalse(board.hasStructureAdjacentToTile(1, 7));
        assertEquals(List.of(0), board.getIncidentEdgeIds(0));
        assertEquals(List.of(0, 1), board.getIncidentEdgeIds(1));
        assertEquals(List.of(0), board.getTileIdsAdjacentToNode(1));
        assertEquals(List.of(1), board.getTileIdsAdjacentToNode(2));
        assertEquals(List.of(1), board.getAdjacentNodeIds(0));
        assertEquals(List.of(0, 2), board.getAdjacentNodeIds(1));
        assertTrue(board.hasAdjacentStructure(0));
        assertFalse(board.hasAdjacentStructure(3));
        assertTrue(board.hasIncidentRoadOwnedBy(1, 7));
        assertFalse(board.hasIncidentRoadOwnedBy(0, 7));
    }
}
