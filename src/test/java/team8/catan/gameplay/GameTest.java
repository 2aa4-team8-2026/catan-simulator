package team8.catan.gameplay;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionTarget;
import team8.catan.actions.ActionType;
import team8.catan.actions.TargetKind;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.dice.Dice;
import team8.catan.dice.TwoDice;
import team8.catan.output.ConsoleActionLogger;
import team8.catan.output.ActionLogger;
import team8.catan.players.Player;
import team8.catan.players.RandomAgent;
import team8.catan.rules.RuleChecker;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameTest {
    @Test
    void Game_run_usesSetupFallbacksDistributesResourcesAndLogsRoundSummary() {
        Board board = new Board(
            List.of(new Node(0), new Node(1), new Node(2), new Node(3)),
            List.of(new Edge(0, 0, 1), new Edge(1, 2, 3))
        );
        ScriptedPlayer player0 = new ScriptedPlayer(
            0,
            Arrays.asList(
                new Action(ActionType.PASS, -1),
                null,
                new Action(ActionType.BUILD_CITY, 0)
            )
        );
        ScriptedPlayer player1 = new ScriptedPlayer(
            1,
            Arrays.asList(
                null,
                new Action(ActionType.BUILD_SETTLEMENT, 2),
                null
            )
        );
        RecordingActionLogger logger = new RecordingActionLogger();
        Game game = new Game(
            board,
            List.of(player0, player1),
            new StubRuleChecker(),
            1,
            10,
            new FixedDice(2, 2),
            logger
        );

        game.run();

        assertEquals(1, game.getRound());
        assertEquals(0, board.getNode(0).getOwnerId());
        assertEquals(1, board.getNode(2).getOwnerId());
        assertEquals(0, board.getEdge(0).getRoadOwnerId());
        assertEquals(1, board.getEdge(1).getRoadOwnerId());
        assertEquals(2, player0.getResourceHand().totalCards());
        assertEquals(2, player1.getResourceHand().totalCards());
        assertEquals(6, logger.loggedActions.size());
        assertEquals(1, logger.roundLogs);
        List<Player> copiedPlayers = game.getPlayers();
        copiedPlayers.clear();
        assertEquals(2, game.getPlayers().size());

        player0.addVictoryPoints(10);
        Game alreadyWon = new Game(
            new Board(List.of(new Node(0)), List.of()),
            List.of(player0),
            new StubRuleChecker(),
            1,
            10,
            new FixedDice(7),
            logger
        );
        assertTrue(alreadyWon.shouldTerminate());

        String consoleOutput = captureStdout(() -> {
            ConsoleActionLogger consoleActionLogger = new ConsoleActionLogger();
            consoleActionLogger.logAction(false, player0, new Action(ActionType.PASS, -1), true);
            consoleActionLogger.logAction(true, player1, new Action(ActionType.BUILD_SETTLEMENT, 2), false);
            consoleActionLogger.logRoundVictoryPoints(1, List.of(player0, player1));
        });
        assertTrue(consoleOutput.contains("TURN|P0|PASS|-1|OK"));
        assertTrue(consoleOutput.contains("SETUP|P1|BUILD_SETTLEMENT|2|REJECTED"));
        assertTrue(consoleOutput.contains("ROUND_END|1|VP|P0=11|P1=1"));

        RandomAgent randomAgent = new RandomAgent(2, new FixedRandom(0, 1));
        Action noBuildAction = randomAgent.chooseAction(
            new Board(List.of(), List.of()),
            new EmptyLegalActionRuleChecker(),
            GamePhase.RUNNING
        );
        assertEquals(ActionType.PASS, noBuildAction.getActionType());
        for (ResourceType type : ResourceType.values()) {
            randomAgent.getResourceHand().add(type, 2);
        }
        Action selectedBuild = randomAgent.chooseAction(
            new Board(List.of(), List.of()),
            new FixedLegalActionRuleChecker(List.of(
                new Action(ActionType.PASS, -1),
                new Action(ActionType.BUILD_SETTLEMENT, 9)
            )),
            GamePhase.RUNNING
        );
        assertEquals(ActionType.BUILD_SETTLEMENT, selectedBuild.getActionType());

        ActionTarget none = ActionTarget.none();
        ActionTarget edgeTarget = ActionTarget.of(TargetKind.EDGE, 3);
        assertEquals(TargetKind.NONE, none.getKind());
        assertEquals(ActionTarget.NO_TARGET_ID, none.getId());
        assertEquals(edgeTarget, ActionTarget.of(TargetKind.EDGE, 3));
        assertEquals(edgeTarget.hashCode(), ActionTarget.of(TargetKind.EDGE, 3).hashCode());
        assertNotEquals(edgeTarget, none);

        Dice fixedRangeDice = new Dice(2, 4, new FixedRandom(0, 2));
        assertEquals(2, fixedRangeDice.roll());
        assertEquals(4, fixedRangeDice.roll());
        assertThrows(IllegalArgumentException.class, () -> new Dice(5, 4, new Random(0)));
        assertEquals(2, new TwoDice(1, 1).roll());
    }

    private static final class ScriptedPlayer extends Player {
        private final List<Action> scriptedActions;
        private int index;

        private ScriptedPlayer(int id, List<Action> scriptedActions) {
            super(id);
            this.scriptedActions = new ArrayList<>(scriptedActions);
        }

        @Override
        public Action chooseAction(Board board, RuleChecker ruleChecker, GamePhase phase) {
            if (index >= scriptedActions.size()) {
                return null;
            }
            return scriptedActions.get(index++);
        }
    }

    private static final class FixedDice extends team8.catan.dice.Dice {
        private final int[] rolls;
        private int index;

        private FixedDice(int... rolls) {
            this.rolls = rolls;
        }

        @Override
        public int roll() {
            int roll = rolls[index % rolls.length];
            index++;
            return roll;
        }
    }

    private static final class RecordingActionLogger implements ActionLogger {
        private final List<Action> loggedActions = new ArrayList<>();
        private int roundLogs;

        @Override
        public void logAction(boolean setupPhase, Player player, Action action, boolean applied) {
            loggedActions.add(action);
        }

        @Override
        public void logRoundVictoryPoints(int round, List<Player> players) {
            roundLogs++;
        }
    }

    private static final class FixedRandom extends Random {
        private final int[] values;
        private int index;

        private FixedRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            int value = values[index % values.length];
            index++;
            return Math.floorMod(value, bound);
        }
    }

    private static final class EmptyLegalActionRuleChecker extends RuleChecker {
        @Override
        public List<Action> getLegalActions(Board board, Player player, GamePhase phase) {
            return List.of();
        }
    }

    private static final class FixedLegalActionRuleChecker extends RuleChecker {
        private final List<Action> actions;

        private FixedLegalActionRuleChecker(List<Action> actions) {
            this.actions = actions;
        }

        @Override
        public List<Action> getLegalActions(Board board, Player player, GamePhase phase) {
            return actions;
        }
    }

    private static String captureStdout(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            action.run();
            return out.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(original);
        }
    }

    private static final class StubRuleChecker extends RuleChecker {
        @Override
        public List<Action> getLegalActions(Board board, Player player, GamePhase phase) {
            if (phase == GamePhase.SETUP_SETTLEMENT) {
                return List.of(new Action(ActionType.BUILD_SETTLEMENT, player.getId() * 2));
            }
            if (phase == GamePhase.SETUP_ROAD) {
                return List.of(new Action(ActionType.BUILD_ROAD, player.getId()));
            }
            return List.of(new Action(ActionType.PASS, -1));
        }

        @Override
        public boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
            return getLegalActions(board, player, phase).contains(action);
        }
    }
}
