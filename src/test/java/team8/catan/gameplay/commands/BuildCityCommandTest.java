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

public class BuildCityCommandTest {
    @Test
    void executeUndoAndRedo_upgradeAndRestoreCityState() {
        Board board = new Board(List.of(new Node(0)), List.of());
        board.getNode(0).restoreState(4, StructureType.SETTLEMENT);

        TestPlayer player = new TestPlayer(4);
        player.addVictoryPoints(1);
        player.grantResource(ResourceType.ORE, 3);
        player.grantResource(ResourceType.GRAIN, 2);

        BuildCityCommand command = new BuildCityCommand(
            board,
            player,
            new Action(ActionType.BUILD_CITY, 0),
            true,
            new ArrayList<RoadPlacement>()
        );

        assertTrue(command.execute());
        assertEquals(StructureType.CITY, board.getNode(0).getStructureType());
        assertEquals(2, player.getVictoryPoints());
        assertEquals(0, player.getTotalResourceCards());

        command.undo();
        assertEquals(StructureType.SETTLEMENT, board.getNode(0).getStructureType());
        assertEquals(1, player.getVictoryPoints());
        assertEquals(5, player.getTotalResourceCards());

        assertTrue(command.redo());
        assertEquals(StructureType.CITY, board.getNode(0).getStructureType());
        assertEquals(2, player.getVictoryPoints());
    }

    @Test
    void execute_rejectsWhenTargetIsNotOwnedSettlement() {
        Board board = new Board(List.of(new Node(0)), List.of());
        board.getNode(0).restoreState(2, StructureType.CITY);
        TestPlayer player = new TestPlayer(4);

        BuildCityCommand command = new BuildCityCommand(
            board,
            player,
            new Action(ActionType.BUILD_CITY, 0),
            true,
            new ArrayList<RoadPlacement>()
        );

        assertFalse(command.execute());
    }

    @Test
    void commandGuardsPreventDoubleExecuteRedoWhileAppliedAndRedoBeforeExecute() {
        Board board = new Board(List.of(new Node(0)), List.of());
        board.getNode(0).restoreState(4, StructureType.SETTLEMENT);
        TestPlayer player = new TestPlayer(4);
        player.grantResource(ResourceType.ORE, 3);
        player.grantResource(ResourceType.GRAIN, 2);

        BuildCityCommand command = new BuildCityCommand(
            board,
            player,
            new Action(ActionType.BUILD_CITY, 0),
            true,
            new ArrayList<RoadPlacement>()
        );

        command.undo();
        assertFalse(command.redo());
        assertTrue(command.execute());
        assertFalse(command.execute());
        assertFalse(command.redo());
    }
}
