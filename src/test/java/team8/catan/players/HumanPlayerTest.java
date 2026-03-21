package team8.catan.players;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.Node;
import team8.catan.gameplay.GamePhase;
import team8.catan.rules.RuleChecker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HumanPlayerTest {
    @Test
    void chooseAction_handlesListAndBuildMenuFlow() {
        RecordingInputPort input = new RecordingInputPort("ls", "b", "x", "s", "oops", "0");
        HumanPlayer player = new HumanPlayer(0, PlayerColor.RED, input, new HumanCommandParser());
        RuleChecker ruleChecker = new FixedRuleChecker(
            List.of(new Action(ActionType.BUILD_SETTLEMENT, 0))
        );
        Board board = new Board(List.of(new Node(0)), List.of());

        Action action = player.chooseAction(board, ruleChecker, GamePhase.RUNNING);

        assertEquals(ActionType.BUILD_SETTLEMENT, action.getActionType());
        assertEquals(0, action.getTargetId());
        assertTrue(input.outputs.stream().anyMatch(line -> line.startsWith("Cards: ")));
        assertTrue(input.outputs.contains("Unknown build type. Use s, c, or r."));
        assertTrue(input.outputs.contains("Node id must be an integer."));
        assertTrue(input.outputs.stream().anyMatch(line -> line.startsWith("Settlement nodes: 0")));
    }

    @Test
    void promptForBuildAction_repromptsOnIllegalChoice() {
        RecordingInputPort input = new RecordingInputPort("s", "0", "1");
        HumanPlayer player = new HumanPlayer(0, PlayerColor.RED, input, new HumanCommandParser());
        RuleChecker ruleChecker = new SelectivelyLegalRuleChecker(
            List.of(
                new Action(ActionType.BUILD_SETTLEMENT, 0),
                new Action(ActionType.BUILD_SETTLEMENT, 1)
            ),
            List.of(new Action(ActionType.BUILD_SETTLEMENT, 1))
        );
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of());

        Action action = player.promptForBuildAction(board, ruleChecker, GamePhase.RUNNING);

        assertEquals(ActionType.BUILD_SETTLEMENT, action.getActionType());
        assertEquals(1, action.getTargetId());
    }

    @Test
    void promptForBuildAction_inSetupRoadPhase_requiresValidRoadEndpoints() {
        RecordingInputPort input = new RecordingInputPort("", "0", "0 2", "0 1");
        HumanPlayer player = new HumanPlayer(0, PlayerColor.RED, input, new HumanCommandParser());
        RuleChecker ruleChecker = new FixedRuleChecker(List.of(new Action(ActionType.BUILD_ROAD, 0)));
        Board board = new Board(List.of(new Node(0), new Node(1), new Node(2)), List.of(new Edge(0, 0, 1)));

        Action action = player.promptForBuildAction(board, ruleChecker, GamePhase.SETUP_ROAD);

        assertEquals(ActionType.BUILD_ROAD, action.getActionType());
        assertEquals(0, action.getTargetId());
        assertTrue(input.outputs.contains("Setup road placement is mandatory. Enter legal road endpoints."));
        assertTrue(input.outputs.contains("Enter exactly 2 node ids, e.g. 12,19"));
        assertTrue(input.outputs.contains("No edge exists between nodes 0 and 2."));
    }

    @Test
    void actionFromBuildCommand_returnsNullForIncompleteOrMissingRoad() {
        RecordingInputPort input = new RecordingInputPort();
        HumanPlayer player = new HumanPlayer(0, PlayerColor.RED, input, new HumanCommandParser());
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of());

        assertNull(player.actionFromBuildCommand(HumanCommand.buildRoad(0, 1), board));
        assertTrue(input.outputs.contains("No edge exists between nodes 0 and 1."));
    }

    @Test
    void humanCommand_executeAction_coversGoUndoRedoRollAndInvalid() {
        RecordingInputPort input = new RecordingInputPort();
        HumanPlayer player = new HumanPlayer(0, PlayerColor.RED, input, new HumanCommandParser());
        Board board = new Board(List.of(new Node(0)), List.of());
        RuleChecker checker = new FixedRuleChecker(List.of());

        assertNull(HumanCommand.roll().executeAction(player, board, checker, GamePhase.RUNNING));
        assertEquals(ActionType.PASS, HumanCommand.go().executeAction(player, board, checker, GamePhase.RUNNING).getActionType());
        assertNull(HumanCommand.go().executeAction(player, board, checker, GamePhase.SETUP_SETTLEMENT));
        assertEquals(ActionType.UNDO, HumanCommand.undo().executeAction(player, board, checker, GamePhase.RUNNING).getActionType());
        assertEquals(ActionType.REDO, HumanCommand.redo().executeAction(player, board, checker, GamePhase.RUNNING).getActionType());
        assertNull(HumanCommand.invalid("bad input").executeAction(player, board, checker, GamePhase.RUNNING));

        assertTrue(input.outputs.contains("Dice auto-rolls at turn start. Use b, ls, or Enter."));
        assertTrue(input.outputs.contains("Setup placement is mandatory. Choose a legal build target."));
        assertTrue(input.outputs.contains("bad input"));
    }

    @Test
    void promptForBuildAction_allowsCancelAtBuildTypeOrTargetInRunningPhase() {
        HumanPlayer playerCancelType = new HumanPlayer(0, PlayerColor.RED, new RecordingInputPort(""), new HumanCommandParser());
        HumanPlayer playerCancelTarget = new HumanPlayer(0, PlayerColor.RED, new RecordingInputPort("s", ""), new HumanCommandParser());
        RuleChecker ruleChecker = new FixedRuleChecker(List.of(new Action(ActionType.BUILD_SETTLEMENT, 0)));
        Board board = new Board(List.of(new Node(0)), List.of());

        assertNull(playerCancelType.promptForBuildAction(board, ruleChecker, GamePhase.RUNNING));
        assertNull(playerCancelTarget.promptForBuildAction(board, ruleChecker, GamePhase.RUNNING));
    }

    @Test
    void promptForBuildAction_forRoad_repromptsOnBadFormattingAndNonIntegerEndpoints() {
        RecordingInputPort input = new RecordingInputPort("r", "0", "a b", "0 1");
        HumanPlayer player = new HumanPlayer(0, PlayerColor.RED, input, new HumanCommandParser());
        RuleChecker ruleChecker = new FixedRuleChecker(List.of(new Action(ActionType.BUILD_ROAD, 0)));
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of(new Edge(0, 0, 1)));

        Action action = player.promptForBuildAction(board, ruleChecker, GamePhase.RUNNING);

        assertEquals(ActionType.BUILD_ROAD, action.getActionType());
        assertTrue(input.outputs.contains("Enter exactly 2 node ids, e.g. 12,19"));
        assertTrue(input.outputs.contains("Node ids must be integers."));
    }

    @Test
    void humanCommand_executeDirectBuild_coversWrongPhaseAndIllegalBuild() {
        RecordingInputPort input = new RecordingInputPort();
        HumanPlayer player = new HumanPlayer(0, PlayerColor.RED, input, new HumanCommandParser());
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of(new Edge(0, 0, 1)));
        RuleChecker ruleChecker = new SelectivelyLegalRuleChecker(
            List.of(new Action(ActionType.BUILD_SETTLEMENT, 1)),
            List.of()
        );

        assertNull(HumanCommand.buildCity(0).executeAction(player, board, ruleChecker, GamePhase.SETUP_SETTLEMENT));
        assertNull(HumanCommand.buildSettlement(0).executeAction(player, board, ruleChecker, GamePhase.RUNNING));

        assertTrue(input.outputs.contains("That build type is not available in this phase."));
        assertTrue(input.outputs.contains("That build is not legal right now."));
        assertTrue(input.outputs.stream().anyMatch(line -> line.startsWith("Settlement nodes: 1")));
    }

    private static final class RecordingInputPort implements HumanInputPort {
        private final Deque<String> inputs = new ArrayDeque<>();
        private final List<String> outputs = new ArrayList<>();

        private RecordingInputPort(String... inputs) {
            for (String input : inputs) {
                this.inputs.addLast(input);
            }
        }

        @Override
        public String readLine(String prompt) {
            outputs.add(prompt);
            return inputs.isEmpty() ? "" : inputs.removeFirst();
        }

        @Override
        public void printLine(String message) {
            outputs.add(message);
        }
    }

    private static class FixedRuleChecker extends RuleChecker {
        private final List<Action> legalActions;

        private FixedRuleChecker(List<Action> legalActions) {
            this.legalActions = legalActions;
        }

        @Override
        public List<Action> getLegalActions(Board board, Player player, GamePhase phase) {
            return legalActions;
        }

        @Override
        public boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
            return legalActions.contains(action);
        }
    }

    private static final class SelectivelyLegalRuleChecker extends FixedRuleChecker {
        private final List<Action> actuallyLegal;

        private SelectivelyLegalRuleChecker(List<Action> legalActions, List<Action> actuallyLegal) {
            super(legalActions);
            this.actuallyLegal = actuallyLegal;
        }

        @Override
        public boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
            return actuallyLegal.contains(action);
        }
    }
}
