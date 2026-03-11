package team8.catan.gameplay;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.output.ActionLogger;
import team8.catan.players.Player;
import team8.catan.rules.RuleChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
