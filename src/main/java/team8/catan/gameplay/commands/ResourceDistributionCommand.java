package team8.catan.gameplay.commands;

import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.board.StructureType;
import team8.catan.players.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResourceDistributionCommand implements UndoableCommand {
    private final Board board;
    private final List<Player> players;
    private final int diceRoll;

    private final Map<Integer, Map<ResourceType, Integer>> previousResources = new LinkedHashMap<>();
    private final Map<Integer, Integer> grants = new LinkedHashMap<>();

    private ResourceType producedType;
    private boolean executedOnce;
    private boolean applied;

    public ResourceDistributionCommand(Board board, List<? extends Player> players, int diceRoll) {
        this.board = Objects.requireNonNull(board, "board");
        this.players = List.copyOf(Objects.requireNonNull(players, "players"));
        this.diceRoll = diceRoll;
    }

    @Override
    public boolean execute() {
        if (executedOnce) {
            return false;
        }

        producedType = resourceTypeForRoll(diceRoll);
        captureInitialState();
        computeGrants();
        applyGrants();
        executedOnce = true;
        applied = true;
        return true;
    }

    @Override
    public void undo() {
        if (!applied) {
            return;
        }

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

        applyGrants();
        applied = true;
        return true;
    }

    private void captureInitialState() {
        previousResources.clear();
        for (Player player : players) {
            previousResources.put(player.getId(), player.getResourceSnapshot());
        }
    }

    private void computeGrants() {
        grants.clear();
        for (Node node : board.getNodes()) {
            if (node.getOwnerId() == Node.UNOWNED || node.getStructureType() == null) {
                continue;
            }

            int amount = node.getStructureType() == StructureType.CITY ? 2 : 1;
            grants.merge(node.getOwnerId(), amount, Integer::sum);
        }
    }

    private void applyGrants() {
        for (Player player : players) {
            int amount = grants.getOrDefault(player.getId(), 0);
            if (amount > 0) {
                player.grantResource(producedType, amount);
            }
        }
    }

    private static ResourceType resourceTypeForRoll(int diceRoll) {
        ResourceType[] all = ResourceType.values();
        int index = Math.floorMod(diceRoll - 2, all.length);
        return all[index];
    }
}
