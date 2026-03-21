package team8.catan.gameplay.commands;

import org.junit.jupiter.api.Test;
import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.board.StructureType;
import team8.catan.support.TestPlayer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceDistributionCommandTest {
    @Test
    void executeUndoAndRedo_restorePlayerResources() {
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of());
        board.getNode(0).restoreState(0, StructureType.SETTLEMENT);
        board.getNode(1).restoreState(1, StructureType.CITY);

        TestPlayer player0 = new TestPlayer(0);
        TestPlayer player1 = new TestPlayer(1);

        ResourceDistributionCommand command = new ResourceDistributionCommand(board, List.of(player0, player1), 2);

        assertTrue(command.execute());
        assertEquals(1, player0.getResourceCount(ResourceType.BRICK));
        assertEquals(2, player1.getResourceCount(ResourceType.BRICK));

        command.undo();
        assertEquals(0, player0.getTotalResourceCards());
        assertEquals(0, player1.getTotalResourceCards());

        assertTrue(command.redo());
        assertEquals(1, player0.getResourceCount(ResourceType.BRICK));
        assertEquals(2, player1.getResourceCount(ResourceType.BRICK));
    }

    @Test
    void guardBranchesPreventReexecuteAndRedoWhileAppliedAndSkipUnownedNodes() {
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of());
        board.getNode(0).restoreState(Node.UNOWNED, null);
        board.getNode(1).restoreState(2, StructureType.SETTLEMENT);
        TestPlayer player = new TestPlayer(2);
        ResourceDistributionCommand command = new ResourceDistributionCommand(board, List.of(player), 3);

        command.undo();
        assertFalse(command.redo());
        assertTrue(command.execute());
        assertFalse(command.execute());
        assertFalse(command.redo());
        assertEquals(1, player.getTotalResourceCards());
    }
}
