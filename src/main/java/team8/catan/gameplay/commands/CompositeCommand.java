package team8.catan.gameplay.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompositeCommand implements UndoableCommand {
    private final List<UndoableCommand> children;

    private boolean executedOnce;
    private boolean applied;

    public CompositeCommand(List<? extends UndoableCommand> children) {
        this.children = new ArrayList<>(Objects.requireNonNull(children, "children"));
    }

    @Override
    public boolean execute() {
        if (executedOnce) {
            return false;
        }

        int appliedChildren = 0;
        for (UndoableCommand child : children) {
            if (!child.execute()) {
                rollbackAppliedChildren(appliedChildren);
                return false;
            }
            appliedChildren++;
        }

        executedOnce = true;
        applied = true;
        return true;
    }

    @Override
    public void undo() {
        if (!applied) {
            return;
        }

        for (int i = children.size() - 1; i >= 0; i--) {
            children.get(i).undo();
        }
        applied = false;
    }

    @Override
    public boolean redo() {
        if (!executedOnce || applied) {
            return false;
        }

        int redoneChildren = 0;
        for (UndoableCommand child : children) {
            if (!child.redo()) {
                rollbackRedoneChildren(redoneChildren);
                return false;
            }
            redoneChildren++;
        }

        applied = true;
        return true;
    }

    protected final List<UndoableCommand> getChildren() {
        return new ArrayList<>(children);
    }

    private void rollbackAppliedChildren(int appliedChildren) {
        for (int i = appliedChildren - 1; i >= 0; i--) {
            children.get(i).undo();
        }
    }

    private void rollbackRedoneChildren(int redoneChildren) {
        for (int i = redoneChildren - 1; i >= 0; i--) {
            children.get(i).undo();
        }
    }
}
