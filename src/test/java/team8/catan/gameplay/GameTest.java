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
import team8.catan.board.Tile;
import team8.catan.dice.Dice;
import team8.catan.dice.TwoDice;
import team8.catan.logging.ActionLogger;
import team8.catan.logging.GameStateWriter;
import team8.catan.logging.JsonStateWriter;
import team8.catan.logging.RoadPlacement;
import team8.catan.players.ConsoleHumanInputPort;
import team8.catan.players.HumanCommand;
import team8.catan.players.HumanCommandParser;
import team8.catan.players.HumanInputPort;
import team8.catan.players.HumanPlayer;
import team8.catan.players.Player;
import team8.catan.players.PlayerColor;
import team8.catan.players.RandomAgent;
import team8.catan.rules.RuleChecker;
import team8.catan.rules.RobberService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameTest {
    @Test
    void Game_run_usesSetupFallbacksDistributesResourcesAndLogsRoundSummary() throws Exception {
        Board board = new Board(
            List.of(new Node(0), new Node(1), new Node(2), new Node(3)),
            List.of(new Edge(0, 0, 1), new Edge(1, 2, 3)),
            List.of(new Tile(0, 0, 0, 0, ResourceType.BRICK, 5, new int[] { 0, 1, 2 })),
            0
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
        RecordingStepForwardGate gate = new RecordingStepForwardGate();
        RecordingStateWriter writer = new RecordingStateWriter();
        Game game = new Game(
            board,
            List.of(player0, player1),
            new StubRuleChecker(),
            1,
            10,
            new FixedDice(2, 2),
            logger,
            gate,
            writer
        );

        game.run();

        assertEquals(1, game.getRound());
        assertEquals(0, board.getNode(0).getOwnerId());
        assertEquals(1, board.getNode(2).getOwnerId());
        assertEquals(0, board.getEdge(0).getRoadOwnerId());
        assertEquals(1, board.getEdge(1).getRoadOwnerId());
        assertEquals(2, player0.getTotalResourceCards());
        assertEquals(2, player1.getTotalResourceCards());
        assertEquals(6, logger.loggedActions.size());
        assertEquals(1, logger.roundLogs);
        assertEquals(6, gate.awaitCalls);
        assertEquals(6, writer.writeCalls);
        assertEquals(2, writer.lastRoadOrder.size());

        player0.addVictoryPoints(10);
        Game alreadyWon = new Game(
            new Board(List.of(new Node(0)), List.of()),
            List.of(player0),
            new StubRuleChecker(),
            1,
            10,
            new FixedDice(7),
            logger,
            new NoOpStepForwardGate(),
            writer
        );
        assertTrue(alreadyWon.shouldTerminate());

        String consoleOutput = captureStdout(() -> {
            new team8.catan.logging.ConsoleActionLogger().logAction(
                1,
                player0,
                new Action(ActionType.BUILD_ROAD, 0),
                false
            );
            new team8.catan.logging.ConsoleActionLogger().logRoundVictoryPoints(1, List.of(player0, player1));
            new team8.catan.output.ConsoleActionLogger().logAction(
                false,
                player0,
                new Action(ActionType.PASS, -1),
                true
            );
            new team8.catan.output.ConsoleActionLogger().logRoundVictoryPoints(1, List.of(player0, player1));
        });
        assertTrue(consoleOutput.contains("attempted to build a road on edge 0"));
        assertTrue(consoleOutput.contains("ROUND_END|1|VP"));

        Path stateFile = Files.createTempFile("catan-state", ".json");
        new JsonStateWriter(stateFile).write(board, List.of(player0, player1), writer.lastRoadOrder);
        String stateJson = Files.readString(stateFile);
        assertTrue(stateJson.contains("\"roads\""));
        assertTrue(stateJson.contains("\"buildings\""));

        RandomAgent randomAgent = new RandomAgent(2, PlayerColor.ORANGE, new FixedRandom(0, 1));
        Action noBuildAction = randomAgent.chooseAction(
            new Board(List.of(), List.of()),
            new EmptyLegalActionRuleChecker(),
            GamePhase.RUNNING
        );
        assertEquals(ActionType.PASS, noBuildAction.getActionType());
        for (ResourceType type : ResourceType.values()) {
            randomAgent.grantResource(type, 2);
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

        RecordingInputPort inputPort = new RecordingInputPort("wait", "g");
        new ConsoleStepForwardGate(inputPort).awaitGo(3, player0, GamePhase.RUNNING);
        assertTrue(inputPort.outputs.contains("Invalid input. Press Enter or type go."));

        RandomAgent roller = new RandomAgent(0, PlayerColor.RED);
        RandomAgent victim = new RandomAgent(1, PlayerColor.BLUE);
        victim.grantResource(ResourceType.WOOL, 8);
        Board robberBoard = new Board(
            List.of(new Node(0), new Node(1)),
            List.of(new Edge(0, 0, 1)),
            List.of(new Tile(0, 0, 0, 0, ResourceType.WOOL, 6, new int[] { 0 })),
            0
        );
        robberBoard.getNode(0).placeSettlement(1);
        new RobberService(new FixedRandom(0, 0, 0, 0, 0, 0)).resolveRobber(roller, robberBoard, List.of(roller, victim));
        assertTrue(roller.getTotalResourceCards() > 0);

        Board humanBoard = new Board(
            List.of(new Node(0), new Node(1), new Node(2)),
            List.of(new Edge(0, 0, 1), new Edge(1, 1, 2))
        );
        HumanRuleChecker humanRuleChecker = new HumanRuleChecker();

        HumanScenario runningScenario = newHumanScenario(3, "ls\nroll\nbuild\nx\nr\n0\n0 2\n0 1\n");
        Action runningAction = runningScenario.player.chooseAction(humanBoard, humanRuleChecker, GamePhase.RUNNING);
        assertEquals(ActionType.BUILD_ROAD, runningAction.getActionType());
        assertEquals(0, runningAction.getTargetId());
        assertTrue(runningScenario.output().contains("Possible actions now: Enter=pass, ls=list, b=build."));
        assertTrue(runningScenario.output().contains("Cards: BRICK=2"));
        assertTrue(runningScenario.output().contains("Dice auto-rolls at turn start. Use b, ls, or Enter."));
        assertTrue(runningScenario.output().contains("Unknown build type. Use s, c, or r."));
        assertTrue(runningScenario.output().contains("Road endpoints: 0-1"));
        assertTrue(runningScenario.output().contains("Enter exactly 2 node ids, e.g. 12,19"));
        assertTrue(runningScenario.output().contains("No edge exists between nodes 0 and 2."));

        HumanScenario setupSettlementScenario = newHumanScenario(4, "\nbuild city 1\nb\n\nabc\n1\n");
        Action setupSettlement = setupSettlementScenario.player.chooseAction(
            humanBoard,
            humanRuleChecker,
            GamePhase.SETUP_SETTLEMENT
        );
        assertEquals(ActionType.BUILD_SETTLEMENT, setupSettlement.getActionType());
        assertEquals(1, setupSettlement.getTargetId());
        assertTrue(setupSettlementScenario.output().contains("Setup placement is mandatory. Choose a legal build target."));
        assertTrue(setupSettlementScenario.output().contains("That build type is not available in this phase."));
        assertTrue(setupSettlementScenario.output().contains("Settlement nodes: 1"));
        assertTrue(setupSettlementScenario.output().contains("Setup settlement placement is mandatory."));
        assertTrue(setupSettlementScenario.output().contains("Node id must be an integer."));

        HumanScenario setupRoadScenario = newHumanScenario(5, "b\n\n1 a\n0 2\n0,1\n");
        Action setupRoad = setupRoadScenario.player.chooseAction(humanBoard, humanRuleChecker, GamePhase.SETUP_ROAD);
        assertEquals(ActionType.BUILD_ROAD, setupRoad.getActionType());
        assertEquals(0, setupRoad.getTargetId());
        assertTrue(setupRoadScenario.output().contains("Road endpoints: 0-1"));
        assertTrue(setupRoadScenario.output().contains("Setup road placement is mandatory. Enter legal road endpoints."));
        assertTrue(setupRoadScenario.output().contains("Node ids must be integers."));
        assertTrue(setupRoadScenario.output().contains("No edge exists between nodes 0 and 2."));

        HumanScenario noBuildScenario = newHumanScenario(6, "g\n");
        Action goAction = noBuildScenario.player.chooseAction(
            humanBoard,
            new EmptyLegalActionRuleChecker(),
            GamePhase.RUNNING
        );
        assertEquals(ActionType.PASS, goAction.getActionType());
        assertTrue(noBuildScenario.output().contains("No legal build actions available."));

        HumanScenario directScenario = newHumanScenario(7, "c\n1\n");
        HumanPlayer directPlayer = directScenario.player;
        assertEquals(
            ActionType.BUILD_SETTLEMENT,
            HumanCommand.buildSettlement(1)
                .executeAction(directPlayer, humanBoard, humanRuleChecker, GamePhase.RUNNING)
                .getActionType()
        );
        assertEquals(
            ActionType.BUILD_CITY,
            HumanCommand.buildCity(1)
                .executeAction(directPlayer, humanBoard, humanRuleChecker, GamePhase.RUNNING)
                .getActionType()
        );
        assertEquals(
            ActionType.BUILD_ROAD,
            HumanCommand.buildRoad(0, 1)
                .executeAction(directPlayer, humanBoard, humanRuleChecker, GamePhase.RUNNING)
                .getActionType()
        );
        assertEquals(
            ActionType.PASS,
            HumanCommand.go().executeAction(directPlayer, humanBoard, humanRuleChecker, GamePhase.RUNNING).getActionType()
        );
        assertNull(HumanCommand.roll().executeAction(directPlayer, humanBoard, humanRuleChecker, GamePhase.RUNNING));
        assertNull(HumanCommand.list().executeAction(directPlayer, humanBoard, humanRuleChecker, GamePhase.RUNNING));
        assertEquals(
            ActionType.BUILD_CITY,
            HumanCommand.buildMenu().executeAction(directPlayer, humanBoard, humanRuleChecker, GamePhase.RUNNING)
                .getActionType()
        );
        assertNull(HumanCommand.invalid("Bad input").executeAction(directPlayer, humanBoard, humanRuleChecker, GamePhase.RUNNING));
        assertNull(HumanCommand.buildSettlement(2).executeAction(directPlayer, humanBoard, humanRuleChecker, GamePhase.RUNNING));
        assertNull(HumanCommand.buildRoad(0, 2).executeAction(directPlayer, humanBoard, humanRuleChecker, GamePhase.RUNNING));
        assertTrue(directScenario.output().contains("That build is not legal right now."));
        assertTrue(directScenario.output().contains("Settlement nodes: 1"));
        assertTrue(directScenario.output().contains("No edge exists between nodes 0 and 2."));
        assertTrue(directScenario.output().contains("Bad input"));

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

    private static HumanScenario newHumanScenario(int id, String inputScript) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleHumanInputPort inputPort = new ConsoleHumanInputPort(
            new Scanner(new ByteArrayInputStream(inputScript.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8),
            new PrintStream(output, true, StandardCharsets.UTF_8)
        );
        HumanPlayer player = new HumanPlayer(id, PlayerColor.values()[id % PlayerColor.values().length], inputPort, new HumanCommandParser());
        for (ResourceType type : ResourceType.values()) {
            player.grantResource(type, 2);
        }
        return new HumanScenario(player, output);
    }

    private static final class ScriptedPlayer extends Player {
        private final List<Action> scriptedActions;
        private int index;

        private ScriptedPlayer(int id, List<Action> scriptedActions) {
            super(id, PlayerColor.values()[id % PlayerColor.values().length]);
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

    private static final class RecordingActionLogger implements ActionLogger {
        private final List<Action> loggedActions = new ArrayList<>();
        private int roundLogs;

        @Override
        public void logAction(int round, Player player, Action action, boolean applied) {
            loggedActions.add(action);
        }

        @Override
        public void logRoundVictoryPoints(int round, List<Player> players) {
            roundLogs++;
        }
    }

    private static final class RecordingStepForwardGate implements StepForwardGate {
        private int awaitCalls;

        @Override
        public void awaitGo(int round, Player player, GamePhase phase) {
            awaitCalls++;
        }
    }

    private static final class RecordingStateWriter implements GameStateWriter {
        private int writeCalls;
        private List<RoadPlacement> lastRoadOrder = List.of();

        @Override
        public void write(Board board, List<Player> players, List<RoadPlacement> roadOrder) {
            writeCalls++;
            lastRoadOrder = new ArrayList<>(roadOrder);
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

    private static final class HumanRuleChecker extends RuleChecker {
        @Override
        public List<Action> getLegalActions(Board board, Player player, GamePhase phase) {
            if (phase == GamePhase.SETUP_SETTLEMENT) {
                return List.of(new Action(ActionType.BUILD_SETTLEMENT, 1));
            }
            if (phase == GamePhase.SETUP_ROAD) {
                return List.of(new Action(ActionType.BUILD_ROAD, 0));
            }
            return List.of(
                new Action(ActionType.PASS, -1),
                new Action(ActionType.BUILD_SETTLEMENT, 1),
                new Action(ActionType.BUILD_CITY, 1),
                new Action(ActionType.BUILD_ROAD, 0)
            );
        }

        @Override
        public boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
            return action != null && getLegalActions(board, player, phase).contains(action);
        }
    }

    private static final class HumanScenario {
        private final HumanPlayer player;
        private final ByteArrayOutputStream output;

        private HumanScenario(HumanPlayer player, ByteArrayOutputStream output) {
            this.player = player;
            this.output = output;
        }

        private String output() {
            return output.toString(StandardCharsets.UTF_8);
        }
    }

    private static final class RecordingInputPort implements HumanInputPort {
        private final List<String> outputs = new ArrayList<>();
        private final List<String> inputs;
        private int index;

        private RecordingInputPort(String... inputs) {
            this.inputs = List.of(inputs);
        }

        @Override
        public String readLine(String prompt) {
            return inputs.get(index++);
        }

        @Override
        public void printLine(String message) {
            outputs.add(message);
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
