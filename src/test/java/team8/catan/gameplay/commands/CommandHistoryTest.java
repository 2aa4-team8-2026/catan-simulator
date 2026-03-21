package team8.catan.gameplay.commands;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandHistoryTest {
    @Test
    void executeUndoAndRedo_manageStacksInOrder() {
        List<String> log = new ArrayList<>();
        CommandHistory history = new CommandHistory();
        UndoableCommand command = new RecordingCommand("first", log);

        assertTrue(history.execute(command));
        assertTrue(history.undo());
        assertTrue(history.redo());

        assertEquals(List.of("execute:first", "undo:first", "redo:first"), log);
    }

    @Test
    void executeAfterUndo_clearsRedoStack() {
        List<String> log = new ArrayList<>();
        CommandHistory history = new CommandHistory();

        assertTrue(history.execute(new RecordingCommand("first", log)));
        assertTrue(history.undo());
        assertTrue(history.execute(new RecordingCommand("second", log)));

        assertFalse(history.redo());
        assertEquals(List.of("execute:first", "undo:first", "execute:second"), log);
    }

    private static final class RecordingCommand implements UndoableCommand {
        private final String name;
        private final List<String> log;
        private boolean executed;
        private boolean applied;

        private RecordingCommand(String name, List<String> log) {
            this.name = name;
            this.log = log;
        }

        @Override
        public boolean execute() {
            if (executed) {
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
            if (!executed || applied) {
                return false;
            }
            applied = true;
            log.add("redo:" + name);
            return true;
        }
    }
}
