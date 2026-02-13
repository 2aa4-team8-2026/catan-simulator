package genaicodebase.output;

import genaicodebase.players.Player;

public class ConsoleActionLogger implements ActionLogger {
    @Override
    public void logTurn(int round, String actionText, int playerId) {
        System.out.println("Round " + round + ": Player " + playerId + " -> " + actionText);
    }

    @Override
    public void logRoundVPs(int round, Player player) {
        if (player == null) {
            return;
        }
        System.out.println("Round " + round + ": Player " + player.getId() + " VP=" + player.getVictoryPoints());
    }
}
