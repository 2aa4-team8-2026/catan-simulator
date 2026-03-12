package team8.catan.rules;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.board.StructureType;
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
        RecordingModule module = new RecordingModule();
        RuleChecker ruleChecker = new RuleChecker(List.of(module));
        Board board = new Board(List.of(new Node(0)), List.of());
        TestPlayer player = new TestPlayer(1);
        Action action = new Action(ActionType.BUILD_SETTLEMENT, 0);

        assertTrue(ruleChecker.isLegal(action, board, player, GamePhase.SETUP_SETTLEMENT));
        assertEquals(2, ruleChecker.getLegalActions(board, player, GamePhase.SETUP_SETTLEMENT).size());
        ruleChecker.onDiceRolled(7, player, board, List.of(player), GamePhase.RUNNING);
        assertEquals(List.of(7), module.rolls);
    }

    @Test
    void RuleChecker_rejectsRunningSettlementWithoutResources() {
        RuleChecker ruleChecker = new RuleChecker();
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of(new Edge(0, 0, 1)));
        TestPlayer player = new TestPlayer(1);
        Action action = new Action(ActionType.BUILD_SETTLEMENT, 0);

        assertFalse(ruleChecker.isLegal(action, board, player, GamePhase.RUNNING));
        assertFalse(ruleChecker.isLegal(null, board, player, GamePhase.RUNNING));
        board.getNode(0).placeSettlement(1);
        player.grantResource(ResourceType.BRICK, 1);
        player.grantResource(ResourceType.LUMBER, 1);
        player.grantResource(ResourceType.WOOL, 1);
        player.grantResource(ResourceType.GRAIN, 1);
        assertTrue(ruleChecker.getLegalActions(board, player, GamePhase.RUNNING).stream()
            .anyMatch(candidate -> candidate.getActionType() == ActionType.BUILD_ROAD));
        assertTrue(new CityRequiresSettlementRuleModule().isLegal(
            new Action(ActionType.BUILD_CITY, 0),
            board,
            player,
            GamePhase.RUNNING
        ));
        board.getNode(0).upgradeToCity();
        assertFalse(new CityRequiresSettlementRuleModule().isLegal(
            new Action(ActionType.BUILD_CITY, 0),
            board,
            player,
            GamePhase.RUNNING
        ));
        board.getNode(1).placeSettlement(2);
        assertEquals(StructureType.CITY, board.getNode(0).getStructureType());
    }

    private static final class RecordingModule implements RuleModule {
        private final List<Integer> rolls = new ArrayList<>();

        @Override
        public boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
            return true;
        }

        @Override
        public void onDiceRolled(int diceRoll, Player roller, Board board, List<? extends Player> players, GamePhase phase) {
            rolls.add(diceRoll);
        }
    }
}
