package genaicodebase.players;

import genaicodebase.gameplay.Action;
import genaicodebase.gameplay.Game;

public interface AgentPolicy {
    Action chooseAction(Game game, Player player);
}
