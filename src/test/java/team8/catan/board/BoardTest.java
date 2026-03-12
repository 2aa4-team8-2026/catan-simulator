package team8.catan.board;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.PlayerColor;
import team8.catan.players.RandomAgent;
import team8.catan.rules.RoadConnectionRuleModule;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardTest {
    @Test
    void Board_hasAdjacentStructureReturnsTrueForNeighborSettlement() {
        Board board = new Board(
            List.of(new Node(0), new Node(1), new Node(2)),
            List.of(new Edge(0, 0, 1), new Edge(1, 1, 2)),
            List.of(new Tile(0, 0, 0, 0, ResourceType.BRICK, 5, new int[] { 0, 1, 2 })),
            0
        );
        board.getNode(1).placeSettlement(2);
        board.getEdge(1).placeRoad(2);

        assertTrue(board.hasAdjacentStructure(0));
        assertEquals(0, board.getEdgeIdBetweenNodes(0, 1));
        assertEquals(0, board.getEdgeIdBetweenNodes(1, 0));
        assertEquals(-1, board.getEdgeIdBetweenNodes(0, 2));
        assertEquals(List.of(0), board.getIncidentEdgeIds(0));
        assertEquals(List.of(0, 2), board.getAdjacentNodeIds(1));
        assertEquals(List.of(0), board.getTileIdsAdjacentToNode(1));
        assertArrayEquals(new int[] { 0, 1, 2 }, board.getAdjacentNodeIdsForTile(0));
        assertTrue(board.hasStructureAdjacentToTile(0, 2));
        assertTrue(board.hasIncidentRoadOwnedBy(2, 2));
        assertFalse(board.hasIncidentRoadOwnedBy(0, 2));
        board.placeRobber(0);
        assertEquals(0, board.getRobberTileId());
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
        assertTrue(board.hasAdjacentStructure(2));
        assertEquals(List.of(0, 2), board.getAdjacentNodeIds(1));
    }
}
