package team8.catan.gameplay.commands;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompositeCommandTest {
    @Test
    void CompositeCommand_executeUndoAndRedo_coverGuardBranches() {
        List<String> log = new ArrayList<>();
        TrackingCommand first = new TrackingCommand("first", log);
        TrackingCommand second = new TrackingCommand("second", log);
        CompositeCommand command = new CompositeCommand(List.of(first, second));

        command.undo();
        assertFalse(command.redo());

        assertTrue(command.execute());
        assertFalse(command.execute());
        assertEquals(List.of("execute:first", "execute:second"), log);

        command.undo();
        assertEquals(List.of("execute:first", "execute:second", "undo:second", "undo:first"), log);

        assertTrue(command.redo());
        assertFalse(command.redo());
        assertEquals(List.of(
            "execute:first",
            "execute:second",
            "undo:second",
            "undo:first",
            "redo:first",
            "redo:second"
        ), log);
    }

    @Test
    void CompositeCommand_execute_rollsBackAlreadyAppliedChildrenWhenLaterChildFails() {
        List<String> log = new ArrayList<>();
        TrackingCommand first = new TrackingCommand("first", log);
        TrackingCommand second = new TrackingCommand("second", log);
        second.executeResult = false;
        CompositeCommand command = new CompositeCommand(List.of(first, second));

        assertFalse(command.execute());

        assertEquals(List.of("execute:first", "undo:first"), log);
    }

    @Test
    void CompositeCommand_redo_rollsBackAlreadyRedoneChildrenWhenLaterChildRedoFails() {
        List<String> log = new ArrayList<>();
        TrackingCommand first = new TrackingCommand("first", log);
        TrackingCommand second = new TrackingCommand("second", log);
        CompositeCommand command = new CompositeCommand(List.of(first, second));

        assertTrue(command.execute());
        command.undo();
        second.redoResult = false;

        assertFalse(command.redo());

        assertEquals(List.of(
            "execute:first",
            "execute:second",
            "undo:second",
            "undo:first",
            "redo:first",
            "undo:first"
        ), log);
    }

    private static final class TrackingCommand implements UndoableCommand {
        private final String name;
        private final List<String> log;
        private boolean executeResult = true;
        private boolean redoResult = true;
        private boolean executed;
        private boolean applied;

        private TrackingCommand(String name, List<String> log) {
            this.name = name;
            this.log = log;
        }

        @Override
        public boolean execute() {
            if (!executeResult || executed) {
                return false;
            }
            executed = true;
            applied = true;
            log.add("execute:" + name);
            return true;
        }

        @Override
        public void undo() {
            if (!applied) {
                return;
            }
            applied = false;
            log.add("undo:" + name);
        }

        @Override
        public boolean redo() {
            if (!executed || applied || !redoResult) {
                return false;
            }
            applied = true;
            log.add("redo:" + name);
            return true;
        }
    }
}
