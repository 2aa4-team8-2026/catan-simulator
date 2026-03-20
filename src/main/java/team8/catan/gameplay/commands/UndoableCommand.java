package team8.catan.gameplay.commands;

public interface UndoableCommand {
    boolean execute();

    void undo();

    boolean redo();
}
