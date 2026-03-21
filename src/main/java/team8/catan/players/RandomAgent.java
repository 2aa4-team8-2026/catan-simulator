package team8.catan.players;

import team8.catan.board.Board;
import team8.catan.actions.Action;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.strategy.AbstractActionSelectionPolicy;
import team8.catan.players.strategy.ImmediateValueSelectionPolicy;
import team8.catan.rules.RuleChecker;

import java.util.Objects;
import java.util.Random;

public class RandomAgent extends Player {
    private final Random random;
    private final AbstractActionSelectionPolicy selectionPolicy;

    public RandomAgent(int id, PlayerColor color) {
        this(id, color, new Random());
    }

    public RandomAgent(int id, PlayerColor color, Random random) {
        this(
            id,
            color,
            random,
            new ImmediateValueSelectionPolicy(random)
        );
    }

    public RandomAgent(int id, PlayerColor color, AbstractActionSelectionPolicy selectionPolicy) {
        this(id, color, new Random(), selectionPolicy);
    }

    public RandomAgent(int id, PlayerColor color, Random random, AbstractActionSelectionPolicy selectionPolicy) {
        super(id, color);
        this.random = Objects.requireNonNull(random, "random");
        this.selectionPolicy = Objects.requireNonNull(selectionPolicy, "selectionPolicy");
    }

    @Override
    public Action chooseAction(Board board, RuleChecker ruleChecker, GamePhase phase) {
        return selectionPolicy.chooseAction(board, this, ruleChecker, phase);
    }
}
