package team8.catan.players.strategy;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;
import team8.catan.rules.RuleChecker;
import team8.catan.support.TestPlayer;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImmediateValueSelectionPolicyTest {
    @Test
    void chooseAction_returnsPassWhenNoLegalActionsExist() {
        ImmediateValueSelectionPolicy policy = new ImmediateValueSelectionPolicy(new Random(0));

        Action chosen = policy.chooseAction(
            new Board(List.of(new Node(0)), List.of()),
            new TestPlayer(0),
            new EmptyRuleChecker(),
            GamePhase.RUNNING
        );

        assertEquals(ActionType.PASS, chosen.getActionType());
    }

    @Test
    void chooseAction_prefersVictoryPointActionOverRoadAndPass() {
        TestPlayer player = new TestPlayer(0);
        player.grantResource(ResourceType.BRICK, 1);
        player.grantResource(ResourceType.LUMBER, 1);
        player.grantResource(ResourceType.WOOL, 1);
        player.grantResource(ResourceType.GRAIN, 1);
        player.grantResource(ResourceType.BRICK, 1);
        player.grantResource(ResourceType.LUMBER, 1);

        ImmediateValueSelectionPolicy policy = new ImmediateValueSelectionPolicy(new Random(0));

        Action chosen = policy.chooseAction(
            new Board(List.of(new Node(0)), List.of()),
            player,
            new FixedLegalActionRuleChecker(List.of(
                new Action(ActionType.PASS, -1),
                new Action(ActionType.BUILD_ROAD, 4),
                new Action(ActionType.BUILD_SETTLEMENT, 2)
            )),
            GamePhase.RUNNING
        );

        assertEquals(ActionType.BUILD_SETTLEMENT, chosen.getActionType());
    }

    @Test
    void chooseAction_breaksTiesOnlyAmongTopScoringActions() {
        TestPlayer player = new TestPlayer(0);
        player.grantResource(ResourceType.BRICK, 1);
        player.grantResource(ResourceType.LUMBER, 1);

        ImmediateValueSelectionPolicy policy = new ImmediateValueSelectionPolicy(new Random(0));

        Action chosen = policy.chooseAction(
            new Board(List.of(new Node(0)), List.of()),
            player,
            new FixedLegalActionRuleChecker(List.of(
                new Action(ActionType.BUILD_ROAD, 5),
                new Action(ActionType.BUILD_ROAD, 7),
                new Action(ActionType.PASS, -1)
            )),
            GamePhase.RUNNING
        );

        assertEquals(ActionType.BUILD_ROAD, chosen.getActionType());
        assertEquals(7, chosen.getTargetId());
    }

    private static final class EmptyRuleChecker extends RuleChecker {
        @Override
        public List<Action> getLegalActions(Board board, Player player, GamePhase phase) {
            return List.of();
        }
    }

    private static final class FixedLegalActionRuleChecker extends RuleChecker {
        private final List<Action> legalActions;

        private FixedLegalActionRuleChecker(List<Action> legalActions) {
            this.legalActions = legalActions;
        }

        @Override
        public List<Action> getLegalActions(Board board, Player player, GamePhase phase) {
            return legalActions;
        }
    }
}
