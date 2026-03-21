package team8.catan.gameplay.commands;

import team8.catan.board.Board;
import team8.catan.board.ResourceType;
import team8.catan.players.Player;
import team8.catan.rules.RobberOutcome;
import team8.catan.rules.RobberService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RobberResolutionCommand implements UndoableCommand {
    private final Board board;
    private final Player roller;
    private final List<Player> players;
    private final RobberService robberService;

    private final Map<Integer, Map<ResourceType, Integer>> previousResources = new LinkedHashMap<>();

    private int previousRobberTileId;
    private RobberOutcome outcome;
    private boolean executedOnce;
    private boolean applied;

    public RobberResolutionCommand(
        Board board,
        Player roller,
        List<? extends Player> players,
        RobberService robberService
    ) {
        this.board = Objects.requireNonNull(board, "board");
        this.roller = Objects.requireNonNull(roller, "roller");
        this.players = List.copyOf(Objects.requireNonNull(players, "players"));
        this.robberService = Objects.requireNonNull(robberService, "robberService");
    }

    @Override
    public boolean execute() {
        if (executedOnce) {
            return false;
        }

        captureInitialState();
        outcome = robberService.resolveOutcome(roller, board, players);
        robberService.applyOutcome(outcome, board, players);
        executedOnce = true;
        applied = true;
        return true;
    }

    @Override
    public void undo() {
        if (!applied) {
            return;
        }

        board.restoreRobberTileId(previousRobberTileId);
        for (Player player : players) {
            player.restoreResourceSnapshot(previousResources.get(player.getId()));
        }
        applied = false;
    }

    @Override
    public boolean redo() {
        if (!executedOnce || applied) {
            return false;
        }

        robberService.applyOutcome(outcome, board, players);
        applied = true;
        return true;
    }

    private void captureInitialState() {
        previousRobberTileId = board.getRobberTileId();
        previousResources.clear();
        for (Player player : players) {
            previousResources.put(player.getId(), player.getResourceSnapshot());
        }
    }
}
