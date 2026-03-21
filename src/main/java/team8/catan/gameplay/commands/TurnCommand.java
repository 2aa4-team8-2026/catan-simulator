package team8.catan.gameplay.commands;

import team8.catan.actions.Action;

public class TurnCommand implements UndoableCommand {
    private final int round;
    private final int activePlayerId;
    private final int diceRoll;
    private final TurnResolutionCommand resolutionCommand;

    private Action chosenAction;
    private UndoableCommand playerActionCommand;
    private boolean executedOnce;
    private boolean applied;

    public TurnCommand(int round, int activePlayerId, int diceRoll, TurnResolutionCommand resolutionCommand) {
        this.round = round;
        this.activePlayerId = activePlayerId;
        this.diceRoll = diceRoll;
        this.resolutionCommand = resolutionCommand;
    }

    @Override
    public boolean execute() {
        if (executedOnce) {
            return false;
        }
        if (!resolutionCommand.execute()) {
            return false;
        }
        executedOnce = true;
        applied = true;
        return true;
    }

    public boolean attachPlayerAction(Action action, UndoableCommand actionCommand) {
        chosenAction = action;
        if (actionCommand == null) {
            return true;
        }
        if (!applied || playerActionCommand != null) {
            return false;
        }
        if (!actionCommand.execute()) {
            return false;
        }
        playerActionCommand = actionCommand;
        return true;
    }

    @Override
    public void undo() {
        if (!applied) {
            return;
        }
        if (playerActionCommand != null) {
            playerActionCommand.undo();
        }
        resolutionCommand.undo();
        applied = false;
    }

    @Override
    public boolean redo() {
        if (!executedOnce || applied) {
            return false;
        }
        if (!resolutionCommand.redo()) {
            return false;
        }
        if (playerActionCommand != null && !playerActionCommand.redo()) {
            resolutionCommand.undo();
            return false;
        }
        applied = true;
        return true;
    }

    public boolean isApplied() {
        return applied;
    }

    public int getRound() {
        return round;
    }

    public int getActivePlayerId() {
        return activePlayerId;
    }

    public int getDiceRoll() {
        return diceRoll;
    }

    public Action getChosenAction() {
        return chosenAction;
    }
}
