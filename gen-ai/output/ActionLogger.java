package genaicodebase.output;

import genaicodebase.players.Player;

public interface ActionLogger {
    void logTurn(int round, String actionText, int playerId);

    void logRoundVPs(int round, Player player);
}
