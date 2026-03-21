package team8.catan.gameplay.commands;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.board.StructureType;
import team8.catan.logging.RoadPlacement;
import team8.catan.support.TestPlayer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuildSettlementCommandTest {
    @Test
    void executeUndoAndRedo_placeAndRestoreSettlementState() {
        Board board = new Board(List.of(new Node(0)), List.of());
        TestPlayer player = new TestPlayer(3);
        player.grantResource(ResourceType.BRICK, 1);
        player.grantResource(ResourceType.LUMBER, 1);
        player.grantResource(ResourceType.WOOL, 1);
        player.grantResource(ResourceType.GRAIN, 1);
        BuildSettlementCommand command = new BuildSettlementCommand(
            board,
            player,
            new Action(ActionType.BUILD_SETTLEMENT, 0),
            true,
            new ArrayList<RoadPlacement>()
        );

        assertTrue(command.execute());
        assertEquals(3, board.getNode(0).getOwnerId());
        assertEquals(StructureType.SETTLEMENT, board.getNode(0).getStructureType());
        assertEquals(1, player.getVictoryPoints());
        assertEquals(0, player.getTotalResourceCards());

        command.undo();
        assertEquals(Node.UNOWNED, board.getNode(0).getOwnerId());
        assertEquals(null, board.getNode(0).getStructureType());
        assertEquals(0, player.getVictoryPoints());
        assertEquals(4, player.getTotalResourceCards());

        assertTrue(command.redo());
        assertEquals(StructureType.SETTLEMENT, board.getNode(0).getStructureType());
    }

    @Test
    void execute_rejectsWhenNodeIsMissingOwnedOrAlreadyStructured() {
        BuildSettlementCommand missing = new BuildSettlementCommand(
            new Board(List.of(), List.of()),
            new TestPlayer(1),
            new Action(ActionType.BUILD_SETTLEMENT, 0),
            true,
            new ArrayList<>()
        );

        Node occupiedNode = new Node(0);
        occupiedNode.placeSettlement(8);
        BuildSettlementCommand occupied = new BuildSettlementCommand(
            new Board(List.of(occupiedNode), List.of()),
            new TestPlayer(1),
            new Action(ActionType.BUILD_SETTLEMENT, 0),
            true,
            new ArrayList<>()
        );

        Node cityNode = new Node(0);
        cityNode.restoreState(Node.UNOWNED, StructureType.CITY);
        BuildSettlementCommand structured = new BuildSettlementCommand(
            new Board(List.of(cityNode), List.of()),
            new TestPlayer(1),
            new Action(ActionType.BUILD_SETTLEMENT, 0),
            true,
            new ArrayList<>()
        );

        assertFalse(missing.execute());
        assertFalse(occupied.execute());
        assertFalse(structured.execute());
    }
}
