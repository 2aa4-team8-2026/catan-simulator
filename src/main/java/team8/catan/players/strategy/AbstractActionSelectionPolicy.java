package team8.catan.players.strategy;

import team8.catan.actions.Action;
import team8.catan.actions.ActionTarget;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;
import team8.catan.rules.RuleChecker;

import java.util.List;

public abstract class AbstractActionSelectionPolicy {
    public final Action chooseAction(Board board, Player player, RuleChecker ruleChecker, GamePhase phase) {
        List<Action> legalActions = getLegalActions(board, player, ruleChecker, phase);
        if (legalActions.isEmpty()) {
            return handleNoLegalActions();
        }
        return selectFromLegalActions(legalActions, player, phase);
    }

    protected List<Action> getLegalActions(Board board, Player player, RuleChecker ruleChecker, GamePhase phase) {
        return ruleChecker.getLegalActions(board, player, phase);
    }

    protected Action handleNoLegalActions() {
        return new Action(ActionType.PASS, ActionTarget.NO_TARGET_ID);
    }

    protected abstract Action selectFromLegalActions(List<Action> legalActions, Player player, GamePhase phase);
}
