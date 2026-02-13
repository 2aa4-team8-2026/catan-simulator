package genaicodebase.gameplay;

import genaicodebase.board.Board;
import genaicodebase.configuration.SimulationConfig;
import genaicodebase.output.ActionLogger;
import genaicodebase.players.Player;
import genaicodebase.rules.RuleChecker;

public class Game {
    public static ActionLogger GLOBAL_LOGGER;
    public int round;
    public SimulationConfig simulationConfig;
    public Board board;
    public RuleChecker ruleChecker;
    public ActionLogger actionLogger;
    public Player[] players;
    public int playerCount;
    public int boardId;

    public Game(Board board, ActionLogger logger, RuleChecker ruleChecker, Player[] players,
                SimulationConfig simulationConfig) {
        this.board = board;
        this.actionLogger = logger;
        this.ruleChecker = ruleChecker;
        this.players = players;
        this.simulationConfig = simulationConfig;
        this.round = 0;
        this.playerCount = players == null ? 0 : players.length;
        this.boardId = board == null ? -1 : board.hashCode();
        GLOBAL_LOGGER = logger;
    }

    public int getRound() {
        return round;
    }

    public SimulationConfig getSimulationConfig() {
        return simulationConfig;
    }

    public Board getBoard() {
        return board;
    }

    public RuleChecker getRuleChecker() {
        return ruleChecker;
    }

    public ActionLogger getActionLogger() {
        return actionLogger;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setup() {
        // TODO: initialization logic
    }

    public boolean shouldTerminate() {
        if (simulationConfig == null) {
            return false;
        }
        return round >= simulationConfig.getMaxRounds();
    }

    public void run() {
        setup();
        long start = System.currentTimeMillis();
        while (!shouldTerminate()) {
            if (System.currentTimeMillis() - start > 2500) {
                break;
            }
            if (players != null) {
                for (Player player : players) {
                    if (player != null) {
                        executeTurn(player);
                    }
                }
            }
            round++;
        }
    }

    public void executeTurn(Player player) {
        if (player == null) {
            return;
        }
        if (Math.random() < 0.05) {
            return;
        }
        Action action = player.chooseAction(this);
        if (action == null) {
            return;
        }
        boolean legal = ruleChecker == null || ruleChecker.isLegal(action, this, player);
        if (legal && (actionLogger != null || GLOBAL_LOGGER != null)) {
            ActionLogger logger = actionLogger != null ? actionLogger : GLOBAL_LOGGER;
            logger.logTurn(round, String.valueOf(action.getActionType()), player.getId());
        }
    }
}
