package team8.catan.rules;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Edge;
import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;
import team8.catan.support.TestPlayer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RuleCheckerTest {
    @Test
    void RuleChecker_allowsSetupSettlementWithoutResources() {
        RuleChecker ruleChecker = new RuleChecker();
        Board board = new Board(List.of(new Node(0)), List.of());
        TestPlayer player = new TestPlayer(1);
        Action action = new Action(ActionType.BUILD_SETTLEMENT, 0);

        assertTrue(ruleChecker.isLegal(action, board, player, GamePhase.SETUP_SETTLEMENT));
    }

    @Test
    void RuleChecker_rejectsRunningSettlementWithoutResources() {
        RuleChecker ruleChecker = new RuleChecker();
        Board board = new Board(List.of(new Node(0)), List.of());
        TestPlayer player = new TestPlayer(1);
        Action action = new Action(ActionType.BUILD_SETTLEMENT, 0);

        assertFalse(ruleChecker.isLegal(action, board, player, GamePhase.RUNNING));
    }

    @Test
    void RuleChecker_rejectsNullControlActionAndInvalidTarget() {
        Board board = new Board(List.of(new Node(0)), List.of());
        TestPlayer player = new TestPlayer(1);
        StubActionGenerationService generation = new StubActionGenerationService(List.of(), List.of());
        RuleChecker ruleChecker = new RuleChecker(generation, List.of());

        assertFalse(ruleChecker.isLegal(null, board, player, GamePhase.RUNNING));
        assertFalse(ruleChecker.isLegal(new Action(ActionType.UNDO, -1), board, player, GamePhase.RUNNING));
        assertFalse(ruleChecker.isLegal(new Action(ActionType.BUILD_SETTLEMENT, 99), board, player, GamePhase.SETUP_SETTLEMENT));
    }

    @Test
    void RuleChecker_allowsPassAndSetupRoadWithoutAffordabilityAndNotifiesModulesOnDiceRoll() {
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of(new Edge(0, 0, 1)));
        TestPlayer player = new TestPlayer(1);
        RecordingRuleModule module = new RecordingRuleModule();
        StubActionGenerationService generation = new StubActionGenerationService(
            List.of(new Action(ActionType.PASS, -1), new Action(ActionType.BUILD_ROAD, 0)),
            List.of(new Action(ActionType.PASS, -1), new Action(ActionType.BUILD_ROAD, 0))
        );
        RuleChecker ruleChecker = new RuleChecker(generation, List.of(module));

        assertTrue(ruleChecker.isLegal(new Action(ActionType.PASS, -1), board, player, GamePhase.RUNNING));
        assertTrue(ruleChecker.isLegal(new Action(ActionType.BUILD_ROAD, 0), board, player, GamePhase.SETUP_ROAD));

        ruleChecker.onDiceRolled(7, player, board, List.of(player), GamePhase.RUNNING);

        assertEquals(1, module.diceRolledCalls);
    }

    @Test
    void RuleChecker_getLegalActionsDeduplicatesAndFiltersModuleRejections() {
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of(new Edge(0, 0, 1)));
        TestPlayer player = new TestPlayer(1);
        player.grantResource(team8.catan.board.ResourceType.BRICK, 1);
        player.grantResource(team8.catan.board.ResourceType.LUMBER, 1);
        player.grantResource(team8.catan.board.ResourceType.WOOL, 1);
        player.grantResource(team8.catan.board.ResourceType.GRAIN, 1);
        RecordingRuleModule module = new RecordingRuleModule();
        module.illegalActions.add(new Action(ActionType.BUILD_ROAD, 0));
        StubActionGenerationService generation = new StubActionGenerationService(
            List.of(
                new Action(ActionType.BUILD_SETTLEMENT, 0),
                new Action(ActionType.BUILD_SETTLEMENT, 0),
                new Action(ActionType.BUILD_ROAD, 0),
                new Action(ActionType.PASS, -1)
            ),
            List.of(
                new Action(ActionType.BUILD_SETTLEMENT, 0),
                new Action(ActionType.BUILD_ROAD, 0),
                new Action(ActionType.PASS, -1)
            )
        );
        RuleChecker ruleChecker = new RuleChecker(generation, List.of(module));

        List<Action> legal = ruleChecker.getLegalActions(board, player, GamePhase.RUNNING);

        assertEquals(List.of(
            new Action(ActionType.BUILD_SETTLEMENT, 0),
            new Action(ActionType.PASS, -1)
        ), legal);
    }

    private static final class StubActionGenerationService extends ActionGenerationService {
        private final List<Action> generated;
        private final List<Action> candidateTargets;

        private StubActionGenerationService(List<Action> generated, List<Action> candidateTargets) {
            this.generated = generated;
            this.candidateTargets = candidateTargets;
        }

        @Override
        public List<Action> generate(Board board, Player player, GamePhase phase) {
            return generated;
        }

        @Override
        public boolean isCandidateTarget(Action action, Board board, Player player, GamePhase phase) {
            return candidateTargets.contains(action);
        }
    }

    private static final class RecordingRuleModule implements RuleModule {
        private final List<Action> illegalActions = new ArrayList<>();
        private int diceRolledCalls;

        @Override
        public boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
            return !illegalActions.contains(action);
        }

        @Override
        public void onDiceRolled(int diceRoll, Player roller, Board board, List<? extends Player> players, GamePhase phase) {
            diceRolledCalls++;
        }
    }
}
