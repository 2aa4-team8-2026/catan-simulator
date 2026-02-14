package team8.catan.players;

import team8.catan.board.Board;
import team8.catan.actions.Action;
import team8.catan.gameplay.GamePhase;
import team8.catan.rules.RuleChecker;

public abstract class Player {
    private final int id;
    private int victoryPoints;
    private final ResourceHand resourceHand;

    protected Player(int id) {
        this.id = id;
        this.victoryPoints = 0;
        this.resourceHand = new ResourceHand();
    }

    public int getId() {
        return id;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }

    public ResourceHand getResourceHand() {
        return resourceHand;
    }

    public void addVictoryPoints(int points) {
        this.victoryPoints += points;
    }

    public void applyActionCost(Action action) {
        resourceHand.spendFor(action.getActionType());
    }

    public abstract Action chooseAction(Board board, RuleChecker ruleChecker, GamePhase phase);
}
