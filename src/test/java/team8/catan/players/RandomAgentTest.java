package team8.catan.players;

import org.junit.jupiter.api.Test;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.strategy.ImmediateValueSelectionPolicy;
import team8.catan.rules.RuleChecker;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RandomAgentTest {
    @Test
    void chooseAction_delegatesToImmediateValueTemplateMethodPolicy() {
        RandomAgent agent = new RandomAgent(
            0,
            PlayerColor.RED,
            new Random(0),
            new ImmediateValueSelectionPolicy(new Random(0))
        );
        seedSettlementResources(agent);
        agent.grantResource(ResourceType.BRICK, 1);
        agent.grantResource(ResourceType.LUMBER, 1);

        Action chosen = agent.chooseAction(
            new Board(List.of(new Node(0)), List.of()),
            new FixedLegalActionRuleChecker(
                List.of(
                    new Action(ActionType.PASS, -1),
                    new Action(ActionType.BUILD_ROAD, 4),
                    new Action(ActionType.BUILD_SETTLEMENT, 2)
                )
            ),
            GamePhase.RUNNING
        );

        assertEquals(ActionType.BUILD_SETTLEMENT, chosen.getActionType());
    }

    private static void seedSettlementResources(Player player) {
        player.grantResource(ResourceType.BRICK, 1);
        player.grantResource(ResourceType.LUMBER, 1);
        player.grantResource(ResourceType.WOOL, 1);
        player.grantResource(ResourceType.GRAIN, 1);
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
