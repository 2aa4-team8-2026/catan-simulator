package team8.catan.gameplay.commands;

import team8.catan.actions.Action;
import team8.catan.board.Board;
import team8.catan.logging.RoadPlacement;
import team8.catan.players.Player;

import java.util.List;
import java.util.Objects;

abstract class AbstractBuildCommand implements UndoableCommand {
    protected final Board board;
    protected final Player player;
    protected final Action action;
    protected final boolean chargeCost;
    protected final List<RoadPlacement> roadPlacementOrder;

    private boolean executedOnce;
    private boolean applied;

    AbstractBuildCommand(
        Board board,
        Player player,
        Action action,
        boolean chargeCost,
        List<RoadPlacement> roadPlacementOrder
    ) {
        this.board = Objects.requireNonNull(board, "board");
        this.player = Objects.requireNonNull(player, "player");
        this.action = Objects.requireNonNull(action, "action");
        this.chargeCost = chargeCost;
        this.roadPlacementOrder = Objects.requireNonNull(roadPlacementOrder, "roadPlacementOrder");
    }

    @Override
    public final boolean execute() {
        if (executedOnce) {
            return false;
        }
        if (!canApplyInitially()) {
            return false;
        }
        captureInitialState();
        applyForward();
        executedOnce = true;
        applied = true;
        return true;
    }

    @Override
    public final void undo() {
        if (!applied) {
            return;
        }
        restoreInitialState();
        applied = false;
    }

    @Override
    public final boolean redo() {
        if (!executedOnce || applied) {
            return false;
        }
        applyForward();
        applied = true;
        return true;
    }

    protected abstract boolean canApplyInitially();

    protected abstract void captureInitialState();

    protected abstract void applyForward();

    protected abstract void restoreInitialState();
}
