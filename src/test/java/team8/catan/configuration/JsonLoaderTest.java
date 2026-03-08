package team8.catan.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonLoaderTest {
    @TempDir
    Path tempDir;

    @Test
    void JsonLoader_readsTurnsAliasIntoMaxRounds() throws IOException {
        Path configFile = tempDir.resolve("game-config.json");
        Files.writeString(configFile, """
            {
              "numPlayers": 4,
              "turns": 25,
              "victoryPointsToWin": 8,
              "startingResourcesPerType": 1
            }
            """);

        GameConfig config = new JsonLoader().load(configFile);

        assertEquals(4, config.getNumPlayers());
        assertEquals(25, config.getMaxRounds());
        assertEquals(8, config.getVictoryPointsToWin());
        assertEquals(1, config.getStartingResourcesPerType());
    }
}
