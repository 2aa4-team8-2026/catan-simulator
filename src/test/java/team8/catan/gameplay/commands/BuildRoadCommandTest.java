package team8.catan.gameplay.commands;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.logging.RoadPlacement;
import team8.catan.support.TestPlayer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuildRoadCommandTest {
    @Test
    void executeUndoAndRedo_placeAndRestoreRoadState() {
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of(new Edge(0, 0, 1)));
        TestPlayer player = new TestPlayer(2);
        player.grantResource(ResourceType.BRICK, 1);
        player.grantResource(ResourceType.LUMBER, 1);
        List<RoadPlacement> roadOrder = new ArrayList<>();
        BuildRoadCommand command = new BuildRoadCommand(
            board,
            player,
            new Action(ActionType.BUILD_ROAD, 0),
            true,
            roadOrder
        );

        assertTrue(command.execute());
        assertEquals(2, board.getEdge(0).getRoadOwnerId());
        assertEquals(0, player.getTotalResourceCards());
        assertEquals(1, roadOrder.size());

        command.undo();
        assertEquals(Edge.UNOWNED, board.getEdge(0).getRoadOwnerId());
        assertEquals(2, player.getTotalResourceCards());
        assertTrue(roadOrder.isEmpty());

        assertTrue(command.redo());
        assertEquals(2, board.getEdge(0).getRoadOwnerId());
        assertEquals(1, roadOrder.size());
    }

    @Test
    void execute_rejectsWhenEdgeIsMissingOrAlreadyOwned() {
        Board missingBoard = new Board(List.of(new Node(0), new Node(1)), List.of());
        BuildRoadCommand missing = new BuildRoadCommand(
            missingBoard,
            new TestPlayer(1),
            new Action(ActionType.BUILD_ROAD, 0),
            true,
            new ArrayList<>()
        );

        Board ownedBoard = new Board(List.of(new Node(0), new Node(1)), List.of(new Edge(0, 0, 1)));
        ownedBoard.getEdge(0).placeRoad(9);
        BuildRoadCommand owned = new BuildRoadCommand(
            ownedBoard,
            new TestPlayer(1),
            new Action(ActionType.BUILD_ROAD, 0),
            true,
            new ArrayList<>()
        );

        assertFalse(missing.execute());
        assertFalse(owned.execute());
    }
}
