package team8.catan.rules;

import team8.catan.board.Board;
import team8.catan.board.ResourceType;
import team8.catan.players.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class RobberService {
    private final Random random;

    public RobberService() {
        this(new Random());
    }

    public RobberService(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    public RobberOutcome resolveOutcome(Player roller, Board board, List<? extends Player> players) {
        Map<Integer, Map<ResourceType, Integer>> snapshots = snapshotPlayers(players);
        discardHalfFromLargeHands(players, snapshots);
        int robberTileId = board.getRandomTileId(random);
        if (robberTileId < 0) {
            return new RobberOutcome(robberTileId, null, null, copySnapshots(snapshots));
        }

        List<Player> victims = new ArrayList<>();
        for (Player player : players) {
            if (player.getId() == roller.getId()) {
                continue;
            }
            if (!board.hasStructureAdjacentToTile(robberTileId, player.getId())) {
                continue;
            }
            if (totalCards(snapshots.get(player.getId())) <= 0) {
                continue;
            }
            victims.add(player);
        }

        if (victims.isEmpty()) {
            return new RobberOutcome(robberTileId, null, null, copySnapshots(snapshots));
        }

        Player victim = victims.get(random.nextInt(victims.size()));
        ResourceType stolen = removeRandomResource(snapshots.get(victim.getId()));
        if (stolen != null) {
            snapshots.get(roller.getId()).merge(stolen, 1, Integer::sum);
        }
        return new RobberOutcome(robberTileId, victim.getId(), stolen, copySnapshots(snapshots));
    }

    public void applyOutcome(RobberOutcome outcome, Board board, List<? extends Player> players) {
        if (outcome.robberTileId() >= 0) {
            board.placeRobber(outcome.robberTileId());
        } else {
            board.restoreRobberTileId(outcome.robberTileId());
        }

        for (Player player : players) {
            Map<ResourceType, Integer> snapshot = outcome.resultingResources().get(player.getId());
            if (snapshot != null) {
                player.restoreResourceSnapshot(snapshot);
            }
        }
    }

    public void resolveRobber(Player roller, Board board, List<? extends Player> players) {
        applyOutcome(resolveOutcome(roller, board, players), board, players);
    }

    private void discardHalfFromLargeHands(
        List<? extends Player> players,
        Map<Integer, Map<ResourceType, Integer>> snapshots
    ) {
        for (Player player : players) {
            Map<ResourceType, Integer> snapshot = snapshots.get(player.getId());
            int total = totalCards(snapshot);
            if (total <= 7) {
                continue;
            }
            int toDiscard = total / 2;
            discardRandomCards(snapshot, toDiscard);
        }
    }

    private int discardRandomCards(Map<ResourceType, Integer> snapshot, int count) {
        int discarded = 0;
        for (int i = 0; i < count; i++) {
            ResourceType removed = removeRandomResource(snapshot);
            if (removed == null) {
                break;
            }
            discarded++;
        }
        return discarded;
    }

    private ResourceType removeRandomResource(Map<ResourceType, Integer> snapshot) {
        int total = totalCards(snapshot);
        if (total <= 0) {
            return null;
        }

        int pick = random.nextInt(total);
        int cumulative = 0;
        for (ResourceType type : ResourceType.values()) {
            int available = snapshot.getOrDefault(type, 0);
            cumulative += available;
            if (pick < cumulative) {
                snapshot.put(type, available - 1);
                return type;
            }
        }

        return null;
    }

    private static int totalCards(Map<ResourceType, Integer> snapshot) {
        int total = 0;
        for (int count : snapshot.values()) {
            total += count;
        }
        return total;
    }

    private static Map<Integer, Map<ResourceType, Integer>> snapshotPlayers(List<? extends Player> players) {
        Map<Integer, Map<ResourceType, Integer>> snapshots = new LinkedHashMap<>();
        for (Player player : players) {
            snapshots.put(player.getId(), player.getResourceSnapshot());
        }
        return snapshots;
    }

    private static Map<Integer, Map<ResourceType, Integer>> copySnapshots(
        Map<Integer, Map<ResourceType, Integer>> snapshots
    ) {
        Map<Integer, Map<ResourceType, Integer>> copy = new LinkedHashMap<>();
        for (Map.Entry<Integer, Map<ResourceType, Integer>> entry : snapshots.entrySet()) {
            copy.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
        }
        return copy;
    }
}
