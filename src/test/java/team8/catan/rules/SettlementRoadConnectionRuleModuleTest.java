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

public class SettlementRoadConnectionRuleModuleTest {
    private final SettlementRoadConnectionRuleModule module = new SettlementRoadConnectionRuleModule();

    @Test
    void SettlementRoadConnectionRuleModule_allowsSetupSettlementWithoutIncidentRoad() {
        Board board = new Board(List.of(new Node(0)), List.of());
        TestPlayer player = new TestPlayer(1);
        Action action = new Action(ActionType.BUILD_SETTLEMENT, 0);

        assertTrue(module.isLegal(action, board, player, GamePhase.SETUP_SETTLEMENT));
    }

    @Test
    void SettlementRoadConnectionRuleModule_rejectsRunningSettlementWithoutIncidentRoad() {
        Board board = new Board(List.of(new Node(0)), List.of());
        TestPlayer player = new TestPlayer(1);
        Action action = new Action(ActionType.BUILD_SETTLEMENT, 0);

        assertFalse(module.isLegal(action, board, player, GamePhase.RUNNING));
    }
}
