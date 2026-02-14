package output;

import actions.Action;
import players.Player;

import java.util.List;

public interface ActionLogger {
    void logAction(boolean setupPhase, Player player, Action action, boolean applied);

    void logRoundVictoryPoints(int round, List<Player> players);
}
