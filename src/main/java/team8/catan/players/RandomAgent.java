package players;

import board.Board;
import actions.Action;
import actions.ActionTarget;
import actions.ActionType;
import gameplay.GamePhase;
import rules.RuleChecker;

import java.util.List;
import java.util.Random;

public class RandomAgent extends Player {
    private final Random random;

    public RandomAgent(int id) {
        this(id, new Random());
    }

    public RandomAgent(int id, Random random) {
        super(id);
        this.random = random;
    }

    @Override
    public Action chooseAction(Board board, RuleChecker ruleChecker, GamePhase phase) {
        List<Action> legalActions = ruleChecker.getLegalActions(board, this, phase);
        if (legalActions.isEmpty()) {
            return new Action(ActionType.PASS, ActionTarget.NO_TARGET_ID);
        }

        if (getResourceHand().totalCards() > 7) {
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
