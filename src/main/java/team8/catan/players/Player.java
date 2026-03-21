package team8.catan.players;

import team8.catan.board.Board;
import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.gameplay.GamePhase;
import team8.catan.rules.RuleChecker;
import team8.catan.board.ResourceType;

import java.util.Map;
import java.util.Random;

public abstract class Player {
    private final int id;
    private final PlayerColor color;
    private int victoryPoints;
    private final ResourceHand resourceHand;

    protected Player(int id, PlayerColor color) {
        this.id = id;
        this.color = color;
        this.victoryPoints = 0;
        this.resourceHand = new ResourceHand();
    }

    public int getId() {
        return id;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }

    public PlayerColor getColor() {
        return color;
    }

    public void addVictoryPoints(int points) {
        this.victoryPoints += points;
    }

    public void restoreVictoryPoints(int victoryPoints) {
        this.victoryPoints = victoryPoints;
    }

    public void applyActionCost(Action action) {
        applyActionCost(action.getActionType());
    }

    public void applyActionCost(ActionType actionType) {
        resourceHand.spendFor(actionType);
    }

    public boolean canAfford(ActionType actionType) {
        return resourceHand.canAfford(actionType);
    }

    public void grantResource(ResourceType type, int amount) {
        resourceHand.add(type, amount);
    }

    public int getResourceCount(ResourceType type) {
        return resourceHand.getCount(type);
    }

    public Map<ResourceType, Integer> getResourceSnapshot() {
        return resourceHand.snapshot();
    }

    public void restoreResourceSnapshot(Map<ResourceType, Integer> snapshot) {
        resourceHand.restore(snapshot);
    }

    public int getTotalResourceCards() {
        return resourceHand.totalCards();
    }

    public int discardRandomResources(int count, Random random) {
        return resourceHand.discardRandomCards(count, random);
    }

    public ResourceType removeRandomResource(Random random) {
        return resourceHand.removeRandomCard(random);
    }

    public abstract Action chooseAction(Board board, RuleChecker ruleChecker, GamePhase phase);
}
