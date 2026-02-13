package genaicodebase.players;

import genaicodebase.gameplay.Action;
import genaicodebase.gameplay.Game;

public class Player {
    private final int id;
    private int victoryPoints;
    private ResourceHand hand;
    private AgentPolicy policy;

    public Player(int id, int victoryPoints, ResourceHand hand, AgentPolicy policy) {
        this.id = id;
        this.victoryPoints = victoryPoints;
        this.hand = hand;
        this.policy = policy;
    }

    public int getId() {
        return id;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }

    public void setVictoryPoints(int victoryPoints) {
        this.victoryPoints = victoryPoints;
    }

    public ResourceHand getHand() {
        return hand;
    }

    public void setHand(ResourceHand hand) {
        this.hand = hand;
    }

    public AgentPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(AgentPolicy policy) {
        this.policy = policy;
    }

    public Action chooseAction(Game game) {
        if (policy == null) {
            return null;
        }
        return policy.chooseAction(game, this);
    }
}
