package team8.catan.gameplay.commands;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.players.Player;
import team8.catan.rules.RobberService;
import team8.catan.support.TestPlayer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TurnCommandTest {
    @Test
    void TurnCommand_executeStoresMetadataAndHandlesPassAction() {
        StubResolutionCommand resolution = new StubResolutionCommand();
        TurnCommand command = new TurnCommand(3, 1, 8, resolution);

        assertFalse(command.redo());
        command.undo();

        assertTrue(command.execute());
        assertTrue(command.isApplied());
        assertEquals(3, command.getRound());
        assertEquals(1, command.getActivePlayerId());
        assertEquals(8, command.getDiceRoll());
        assertTrue(command.attachPlayerAction(new Action(ActionType.PASS, -1), null));
        assertEquals(ActionType.PASS, command.getChosenAction().getActionType());
        assertFalse(command.execute());
    }

    @Test
    void TurnCommand_attachPlayerAction_rejectsInvalidStatesAndFailedActionExecution() {
        StubResolutionCommand resolution = new StubResolutionCommand();
        TurnCommand command = new TurnCommand(1, 0, 6, resolution);
        StubActionCommand failingAction = new StubActionCommand();
        failingAction.executeResult = false;
        Action action = new Action(ActionType.BUILD_ROAD, 0);

        assertFalse(command.attachPlayerAction(action, new StubActionCommand()));
        assertEquals(action, command.getChosenAction());

        assertTrue(command.execute());
        assertFalse(command.attachPlayerAction(action, failingAction));
        assertEquals(action, command.getChosenAction());

        assertTrue(command.attachPlayerAction(action, new StubActionCommand()));
        assertFalse(command.attachPlayerAction(action, new StubActionCommand()));
    }

    @Test
    void TurnCommand_executeAndRedo_coverFailurePathsAndRollback() {
        StubResolutionCommand resolutionFail = new StubResolutionCommand();
        resolutionFail.executeResult = false;
        assertFalse(new TurnCommand(1, 0, 6, resolutionFail).execute());

        StubResolutionCommand resolution = new StubResolutionCommand();
        TurnCommand command = new TurnCommand(2, 0, 9, resolution);
        StubActionCommand action = new StubActionCommand();

        assertTrue(command.execute());
        assertTrue(command.attachPlayerAction(new Action(ActionType.BUILD_CITY, 0), action));

        command.undo();
        assertFalse(command.isApplied());
        assertEquals(List.of("undo"), action.log);
        assertEquals(List.of("execute", "undo"), resolution.log);

        resolution.redoResult = false;
        assertFalse(command.redo());
        resolution.redoResult = true;
        action.redoResult = false;
        assertFalse(command.redo());
        assertEquals(List.of("execute", "undo", "redo", "undo"), resolution.log);

        action.redoResult = true;
        assertTrue(command.redo());
        assertTrue(command.isApplied());
        assertEquals(List.of("undo", "redo"), action.log);
    }

    private static final class StubResolutionCommand extends TurnResolutionCommand {
        private boolean executeResult = true;
        private boolean redoResult = true;
        private boolean executed;
        private boolean applied;
        private final List<String> log = new java.util.ArrayList<>();

        private StubResolutionCommand() {
            super(new Board(List.of(), List.of()), player(), List.of(player()), 6, new RobberService());
        }

        private static Player player() {
            return new TestPlayer(0);
        }

        @Override
        public boolean execute() {
            if (!executeResult || executed) {
                return false;
            }
            executed = true;
            applied = true;
            log.add("execute");
            return true;
        }

        @Override
        public void undo() {
            if (!applied) {
                return;
            }
            applied = false;
            log.add("undo");
        }

        @Override
        public boolean redo() {
            if (!executed || applied || !redoResult) {
                return false;
            }
            applied = true;
            log.add("redo");
            return true;
        }
    }

    private static final class StubActionCommand implements UndoableCommand {
        private boolean executeResult = true;
        private boolean redoResult = true;
        private boolean executed;
        private boolean applied;
        private final List<String> log = new java.util.ArrayList<>();

        @Override
        public boolean execute() {
            if (!executeResult || executed) {
                return false;
            }
            executed = true;
            applied = true;
            return true;
        }

        @Override
        public void undo() {
            if (!applied) {
                return;
            }
            applied = false;
            log.add("undo");
        }

        @Override
        public boolean redo() {
            if (!executed || applied || !redoResult) {
                return false;
            }
            applied = true;
            log.add("redo");
            return true;
        }
    }
}
