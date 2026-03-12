package team8.catan.gameplay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import team8.catan.board.ResourceType;
import team8.catan.configuration.GameConfig;
import team8.catan.configuration.JsonLoader;
import team8.catan.output.ActionLogger;
import team8.catan.players.Player;
import team8.catan.players.RandomAgent;
import team8.catan.rules.RuleChecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class GameFactoryTest {
    @TempDir
    Path tempDir;

    @Test
    void GameFactory_createGame_buildsConfiguredPlayersWithSeededResources() throws IOException {
        Path configPath = tempDir.resolve("game-config.json");
        Files.writeString(configPath, """
            {
              "numPlayers": 3,
              "maxRounds": 5,
              "victoryPointsToWin": 8,
              "startingResourcesPerType": 2
            }
            """);

        GameFactory factory = new GameFactory(new JsonLoader(), new RuleChecker(List.of()), new NoOpActionLogger());
        Game game = factory.createGame(configPath);

        List<Player> players = game.getPlayers();

        assertEquals(3, players.size());
        assertEquals(0, players.get(0).getId());
        assertEquals(1, players.get(1).getId());
        assertEquals(2, players.get(2).getId());
        assertInstanceOf(RandomAgent.class, players.get(0));
        assertInstanceOf(RandomAgent.class, players.get(1));
        assertInstanceOf(RandomAgent.class, players.get(2));
        assertEquals(ResourceType.values().length * 2, players.get(0).getResourceHand().totalCards());
        assertEquals(ResourceType.values().length * 2, players.get(1).getResourceHand().totalCards());
        assertEquals(ResourceType.values().length * 2, players.get(2).getResourceHand().totalCards());
        assertEquals(0, game.getRound());
        assertFalse(game.shouldTerminate());
        Game directConfigGame = factory.createGame(new GameConfig(2, 3, 4, 1));
        assertEquals(2, directConfigGame.getPlayers().size());
        assertEquals(ResourceType.values().length, directConfigGame.getPlayers().get(0).getResourceHand().totalCards());
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
