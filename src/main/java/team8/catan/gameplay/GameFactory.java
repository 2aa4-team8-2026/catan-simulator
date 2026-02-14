package gameplay;

import board.Board;
import board.Edge;
import board.Node;
import board.ResourceType;
import configuration.GameConfig;
import configuration.GameConfigLoader;
import configuration.JsonLoader;
import dice.TwoDice;
import output.ActionLogger;
import output.ConsoleActionLogger;
import players.Player;
import players.RandomAgent;
import rules.RuleChecker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GameFactory {
    private final GameConfigLoader configLoader;
    private final RuleChecker ruleChecker;
    private final ActionLogger actionLogger;

    public GameFactory() {
        this(new JsonLoader(), new RuleChecker(), new ConsoleActionLogger());
    }

    public GameFactory(GameConfigLoader configLoader, RuleChecker ruleChecker) {
        this(configLoader, ruleChecker, new ConsoleActionLogger());
    }

    public GameFactory(GameConfigLoader configLoader, RuleChecker ruleChecker, ActionLogger actionLogger) {
        this.configLoader = Objects.requireNonNull(configLoader, "configLoader");
        this.ruleChecker = Objects.requireNonNull(ruleChecker, "ruleChecker");
        this.actionLogger = Objects.requireNonNull(actionLogger, "actionLogger");
    }

    public Game createGame(Path configPath) throws IOException {
        GameConfig config = configLoader.load(configPath);
        return createGame(config);
    }

    public Game createGame(GameConfig config) {
        Board board = buildBoard(config);
        List<Player> players = buildPlayers(config.getNumPlayers());
        seedStartingResources(players, config.getStartingResourcesPerType());

        return new Game(
            board,
            players,
            ruleChecker,
            config.getMaxRounds(),
            config.getVictoryPointsToWin(),
            new TwoDice(),
            actionLogger
        );
    }

    private Board buildBoard(GameConfig config) {
        // Fixed deterministic preset: 12-node ring with 12 edges.
        // This is stable for testing and supports setup for 4 players.
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        int nodeCount = 12;

        for (int i = 0; i < nodeCount; i++) {
            nodes.add(new Node(i));
        }

        for (int i = 0; i < nodeCount; i++) {
            int next = (i + 1) % nodeCount;
            edges.add(new Edge(i, i, next));
        }

        return new Board(nodes, edges);
    }

    private List<Player> buildPlayers(int numPlayers) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            players.add(new RandomAgent(i));
        }
        return players;
    }

    private void seedStartingResources(List<Player> players, int perResource) {
        for (Player player : players) {
            for (ResourceType type : ResourceType.values()) {
                player.getResourceHand().add(type, perResource);
            }
        }
    }
}
