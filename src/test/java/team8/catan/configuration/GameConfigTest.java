package team8.catan.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameConfigTest {
    @Test
    void GameConfig_allowsMinRoundsBoundary() {
        GameConfig config = assertDoesNotThrow(() -> new GameConfig(4, 1, 10, 2, 1));

        assertEquals(1, config.getMaxRounds());
        assertEquals(1, config.getHumanPlayerIndex());
        assertEquals("base_map.json", config.getBaseMapPath());
        assertEquals("state.json", config.getStatePath());
    }

    @Test
    void GameConfig_allowsMaxRoundsBoundary() {
        GameConfig config = assertDoesNotThrow(() -> new GameConfig(4, 8192, 10, 2, null, "board.json", "state.json"));

        assertEquals(8192, config.getMaxRounds());
        assertEquals("board.json", config.getBaseMapPath());
        assertEquals("state.json", config.getStatePath());
    }

    @Test
    void GameConfig_rejectsRoundsAboveMaxBoundary() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(4, 8193, 10, 2));
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(0, 1, 10, 2));
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(4, 1, 0, 2));
    }

    @Test
    void GameConfig_rejectsRoundsBelowMinBoundary() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(4, 0, 10, 2));
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(4, 1, 10, -1));
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(4, 1, 10, 2, 4));
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(4, 1, 10, 2, null, " ", "state.json"));
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(4, 1, 10, 2, null, "board.json", ""));
    }
}
