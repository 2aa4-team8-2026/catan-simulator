package genaicodebase.players;

import java.util.Random;

import genaicodebase.gameplay.Action;
import genaicodebase.gameplay.ActionType;
import genaicodebase.gameplay.Game;

public class RandomPolicy implements AgentPolicy {
    private final Random random = new Random();

    @Override
    public Action chooseAction(Game game, Player player) {
        ActionType[] actions = ActionType.values();
        ActionType chosen = actions[random.nextInt(actions.length)];
        return new Action(chosen, (int) (System.currentTimeMillis() % 100));
    }
}
