package team8.catan.players.strategy;

import team8.catan.actions.Action;
import team8.catan.actions.ActionTarget;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;
import team8.catan.rules.RuleChecker;

import java.util.List;
import java.util.Objects;

public class ValueBasedActionSelectionPolicy implements ActionSelectionPolicy {
    private final ActionValuationStrategy valuationStrategy;

    public ValueBasedActionSelectionPolicy(ActionValuationStrategy valuationStrategy) {
        this.valuationStrategy = Objects.requireNonNull(valuationStrategy, "valuationStrategy");
    }

    @Override
    public Action chooseAction(Board board, Player player, RuleChecker ruleChecker, GamePhase phase) {
        List<Action> legalActions = ruleChecker.getLegalActions(board, player, phase);
        if (legalActions.isEmpty()) {
            return new Action(ActionType.PASS, ActionTarget.NO_TARGET_ID);
        }
        return valuationStrategy.chooseBest(legalActions, player, phase);
    }
}
