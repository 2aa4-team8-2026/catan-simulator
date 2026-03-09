package team8.catan.output;

import team8.catan.actions.Action;
import team8.catan.players.Player;

import java.util.List;

public final class ConsoleActionLogger implements ActionLogger {
    @Override
    public void logAction(boolean setupPhase, Player player, Action action, boolean applied) {
        String phaseLabel = setupPhase ? "SETUP" : "TURN";
        String status = applied ? "OK" : "REJECTED";
        System.out.println(
            phaseLabel
                + "|P" + player.getId()
                + "|" + action.getActionType()
                + "|" + action.getTargetId()
                + "|" + status
        );
    }

    @Override
    public void logRoundVictoryPoints(int round, List<Player> players) {
        StringBuilder sb = new StringBuilder("ROUND_END|" + round + "|VP");
        for (Player player : players) {
            sb.append("|P")
                .append(player.getId())
                .append("=")
                .append(player.getVictoryPoints());
        }
        System.out.println(sb);
    }
}
