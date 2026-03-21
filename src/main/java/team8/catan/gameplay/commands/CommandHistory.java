package team8.catan.gameplay.commands;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandHistory {
    private final Deque<UndoableCommand> undoStack = new ArrayDeque<>();
    private final Deque<UndoableCommand> redoStack = new ArrayDeque<>();

    public boolean execute(UndoableCommand command) {
        if (!command.execute()) {
            return false;
        }
        undoStack.push(command);
        redoStack.clear();
        return true;
    }

    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        UndoableCommand command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        UndoableCommand command = redoStack.pop();
        if (!command.redo()) {
            redoStack.push(command);
            return false;
        }
        undoStack.push(command);
        return true;
    }
}
