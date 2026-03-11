package team8.catan.gameplay;

import org.junit.jupiter.api.Test;
import team8.catan.board.ResourceType;
import team8.catan.configuration.GameConfig;
import team8.catan.configuration.GameConfigLoader;
import team8.catan.output.ActionLogger;
import team8.catan.players.Player;
import team8.catan.rules.RuleChecker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GameFactoryTest {
    @Test
    void GameFactory_createGame_buildsConfiguredPlayersWithSeededResources() {
        GameFactory factory = new GameFactory(new UnusedLoader(), new RuleChecker(List.of()), new NoOpActionLogger());
        Game game = factory.createGame(new GameConfig(3, 5, 8, 2));

        List<Player> players = game.getPlayers();

        assertEquals(3, players.size());
        assertEquals(0, players.get(0).getId());
        assertEquals(1, players.get(1).getId());
        assertEquals(2, players.get(2).getId());
        assertEquals(ResourceType.values().length * 2, players.get(0).getResourceHand().totalCards());
        assertEquals(ResourceType.values().length * 2, players.get(1).getResourceHand().totalCards());
        assertEquals(ResourceType.values().length * 2, players.get(2).getResourceHand().totalCards());
        assertEquals(0, game.getRound());
        assertFalse(game.shouldTerminate());
    }

    private static final class UnusedLoader extends GameConfigLoader {
        @Override
        public GameConfig load(Path path) throws IOException {
            throw new UnsupportedOperationException("Not used in this test");
        }
    }

    private static final class NoOpActionLogger implements ActionLogger {
        @Override
        public void logAction(boolean setupPhase, Player player, team8.catan.actions.Action action, boolean applied) {
        }

        @Override
        public void logRoundVictoryPoints(int round, List<Player> players) {
        }
    }
}
