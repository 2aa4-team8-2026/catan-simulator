package team8.catan.rules;

import team8.catan.board.Board;
import team8.catan.board.ResourceType;
import team8.catan.players.Player;

import java.util.ArrayList;
import java.util.List;
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

    public void resolveRobber(Player roller, Board board, List<? extends Player> players) {
        discardHalfFromLargeHands(players);

        int robberTileId = board.getRandomTileId(random);
        if (robberTileId < 0) {
            return;
        }
        board.placeRobber(robberTileId);

        List<Player> victims = new ArrayList<>();
        for (Player player : players) {
            if (player.getId() == roller.getId()) {
                continue;
            }
            if (!board.hasStructureAdjacentToTile(robberTileId, player.getId())) {
                continue;
            }
            if (player.getTotalResourceCards() <= 0) {
                continue;
            }
            victims.add(player);
        }

        if (victims.isEmpty()) {
            return;
        }

        Player victim = victims.get(random.nextInt(victims.size()));
        ResourceType stolen = victim.removeRandomResource(random);
        if (stolen != null) {
            roller.grantResource(stolen, 1);
        }
    }

    private void discardHalfFromLargeHands(List<? extends Player> players) {
        for (Player player : players) {
            int total = player.getTotalResourceCards();
            if (total <= 7) {
                continue;
            }
            int toDiscard = total / 2;
            player.discardRandomResources(toDiscard, random);
        }
    }
}
