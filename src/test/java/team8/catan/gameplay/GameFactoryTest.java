package team8.catan.gameplay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import team8.catan.board.ResourceType;
import team8.catan.configuration.GameConfig;
import team8.catan.configuration.JsonLoader;
import team8.catan.io.PathResolver;
import team8.catan.io.TextResourceLoader;
import team8.catan.logging.ActionLogger;
import team8.catan.players.HumanPlayer;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameFactoryTest {
    @TempDir
    Path tempDir;

    @Test
    void GameFactory_createGame_buildsConfiguredPlayersWithSeededResources() throws IOException {
        Path baseMap = tempDir.resolve("base_map.json");
        Files.writeString(
            baseMap,
            Files.readString(Path.of("src/main/resources/team8/catan/game/base_map.json"))
        );
        Path configPath = tempDir.resolve("game-config.json");
        Files.writeString(configPath, """
            {
              "numPlayers": 3,
              "maxRounds": 5,
              "victoryPointsToWin": 8,
              "startingResourcesPerType": 2,
              "humanPlayerIndex": 1,
              "baseMapPath": "base_map.json",
              "statePath": "state/output.json"
            }
            """);

        GameFactory factory = new GameFactory(new JsonLoader(), new RuleChecker(List.of()), new SilentLogger());
        Game game = factory.createGame(configPath);

        List<Player> players = game.getPlayers();

        assertEquals(3, players.size());
        assertEquals(0, players.get(0).getId());
        assertEquals(1, players.get(1).getId());
        assertEquals(2, players.get(2).getId());
        assertInstanceOf(RandomAgent.class, players.get(0));
        assertInstanceOf(HumanPlayer.class, players.get(1));
        assertInstanceOf(RandomAgent.class, players.get(2));
        assertEquals(ResourceType.values().length * 2, players.get(0).getTotalResourceCards());
        assertEquals(ResourceType.values().length * 2, players.get(1).getTotalResourceCards());
        assertEquals(ResourceType.values().length * 2, players.get(2).getTotalResourceCards());
        assertEquals(0, game.getRound());
        assertFalse(game.shouldTerminate());

        GameConfig parsed = new JsonLoader().load(configPath);
        assertEquals(1, parsed.getHumanPlayerIndex());
        assertEquals("base_map.json", parsed.getBaseMapPath());
        assertEquals("state/output.json", parsed.getStatePath());

        PathResolver resolver = new PathResolver();
        assertEquals(baseMap.normalize(), resolver.resolveInputPath(tempDir, "base_map.json"));
        assertEquals(
            tempDir.resolve("state/output.json").normalize(),
            resolver.resolveOutputPath(tempDir, "state/output.json")
        );

        assertTrue(TextResourceLoader.load(baseMap, "base_map.json", getClass()).contains("\"resource\""));
        assertTrue(TextResourceLoader.load(
            tempDir.resolve("missing.json"),
            "team8/catan/config/game-config.json",
            getClass()
        ).contains("\"numPlayers\""));
        String classpathConfig = TextResourceLoader.load(
            Path.of("team8/catan/config/game-config.json"),
            "missing.json",
            getClass()
        );
        assertTrue(classpathConfig.contains("\"maxRounds\"") || classpathConfig.contains("\"turns\""));
        assertThrows(IOException.class, () -> TextResourceLoader.load(
            tempDir.resolve("missing.txt"),
            "missing.txt",
            getClass()
        ));

        GameConfig defaults = new GameConfig(2, 3, 4, 1, 1);
        assertEquals("base_map.json", defaults.getBaseMapPath());
        assertEquals("state.json", defaults.getStatePath());
    }

    private static final class SilentLogger implements ActionLogger {
        @Override
        public void logAction(int round, Player player, team8.catan.actions.Action action, boolean applied) {
            // no-op
        }

        @Override
        public void logRoundVictoryPoints(int round, List<Player> players) {
            // no-op
        }
    }
}
