package team8.catan.logging;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.support.TestPlayer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsoleActionLoggerTest {
    @Test
    void logActionLogRoundVictoryPointsAndInfo_printExpectedMessages() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
        try {
            ConsoleActionLogger logger = new ConsoleActionLogger();
            TestPlayer player = new TestPlayer(2);
            player.addVictoryPoints(3);

            logger.logAction(4, player, new Action(ActionType.PASS, -1), true);
            logger.logAction(4, player, new Action(ActionType.BUILD_ROAD, 1), false);
            logger.logRoundVictoryPoints(4, List.of(player));
            logger.logInfo("redo applied");
        } finally {
            System.setOut(original);
        }

        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("[4] / P2: passed"));
        assertTrue(output.contains("attempted to build a road on edge 1, but it was rejected"));
        assertTrue(output.contains("finished round 4 with 3 victory points"));
        assertTrue(output.contains("[info] redo applied"));
    }
}
