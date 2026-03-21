package team8.catan.logging;

import team8.catan.actions.Action;
import team8.catan.players.Player;

import java.util.List;

public class ConsoleActionLogger implements ActionLogger {
    @Override
    public void logAction(int round, Player player, Action action, boolean applied) {
        String actionDescription = action.getActionType().describe(action.getTargetId());
        if (applied) {
            System.out.println(prefix(round, player) + toPastTense(actionDescription));
            return;
        }
        System.out.println(prefix(round, player) + "attempted to " + actionDescription + ", but it was rejected");
    }

    @Override
    public void logRoundVictoryPoints(int round, List<Player> players) {
        for (Player player : players) {
            System.out.println(
                prefix(round, player)
                    + "finished round "
                    + round
                    + " with "
                    + player.getVictoryPoints()
                    + " victory points"
            );
        }
    }

    @Override
    public void logInfo(String message) {
        System.out.println("[info] " + message);
    }

    private static String prefix(int round, Player player) {
        return "[" + round + "] / P" + player.getId() + ": ";
    }

    private static String toPastTense(String description) {
        if (description.equals("pass")) {
            return "passed";
        }
        if (description.startsWith("build ")) {
            return "built " + description.substring("build ".length());
        }
        return description;
    }
}
