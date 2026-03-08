package team8.catan.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameConfigTest {
    @Test
    void GameConfig_allowsMinRoundsBoundary() {
        GameConfig config = assertDoesNotThrow(() -> new GameConfig(4, 1, 10, 2));

        assertEquals(1, config.getMaxRounds());
    }

    @Test
    void GameConfig_allowsMaxRoundsBoundary() {
        GameConfig config = assertDoesNotThrow(() -> new GameConfig(4, 8192, 10, 2));

        assertEquals(8192, config.getMaxRounds());
    }

    @Test
    void GameConfig_rejectsRoundsAboveMaxBoundary() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(4, 8193, 10, 2));
    }
}
