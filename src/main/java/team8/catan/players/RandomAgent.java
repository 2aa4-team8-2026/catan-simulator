package team8.catan.players;

import team8.catan.board.Board;
import team8.catan.actions.Action;
import team8.catan.actions.ActionTarget;
import team8.catan.actions.ActionType;
import team8.catan.gameplay.GamePhase;
import team8.catan.rules.RuleChecker;

import java.util.List;
import java.util.Random;

public class RandomAgent extends Player {
    private final Random random;

    public RandomAgent(int id, PlayerColor color) {
        this(id, color, new Random());
    }

    public RandomAgent(int id, PlayerColor color, Random random) {
        super(id, color);
        this.random = random;
    }

    @Override
    public Action chooseAction(Board board, RuleChecker ruleChecker, GamePhase phase) {
        List<Action> legalActions = ruleChecker.getLegalActions(board, this, phase);
        if (legalActions.isEmpty()) {
            return new Action(ActionType.PASS, ActionTarget.NO_TARGET_ID);
        }

        if (getTotalResourceCards() > 7) {
            List<Action> buildActions = legalActions.stream()
                .filter(action -> action.getActionType() != ActionType.PASS)
                .toList();
            if (!buildActions.isEmpty()) {
                return buildActions.get(random.nextInt(buildActions.size()));
            }
        }

        return legalActions.get(random.nextInt(legalActions.size()));
    }
}
