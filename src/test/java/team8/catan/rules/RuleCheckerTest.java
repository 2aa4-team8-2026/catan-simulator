package team8.catan.rules;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.gameplay.GamePhase;
import team8.catan.support.TestPlayer;

import java.util.List;

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
}
