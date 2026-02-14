package team8.catan.output;

import team8.catan.actions.Action;
import team8.catan.players.Player;

import java.util.List;

public interface ActionLogger {
    void logAction(boolean setupPhase, Player player, Action action, boolean applied);

    void logRoundVictoryPoints(int round, List<Player> players);
}
