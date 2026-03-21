package team8.catan.gameplay;

import team8.catan.board.Board;
import team8.catan.board.BaseMapLoader;
import team8.catan.board.BaseMapTileSpec;
import team8.catan.board.BoardFactory;
import team8.catan.board.ResourceType;
import team8.catan.configuration.GameConfig;
import team8.catan.configuration.GameConfigLoader;
import team8.catan.configuration.JsonLoader;
import team8.catan.dice.TwoDice;
import team8.catan.io.PathResolver;
import team8.catan.logging.ActionLogger;
import team8.catan.logging.ConsoleActionLogger;
import team8.catan.logging.GameStateWriter;
import team8.catan.logging.JsonStateWriter;
import team8.catan.players.ConsoleHumanInputPort;
import team8.catan.players.HumanCommandParser;
import team8.catan.players.HumanInputPort;
import team8.catan.players.HumanPlayer;
import team8.catan.players.Player;
import team8.catan.players.PlayerColor;
import team8.catan.players.RandomAgent;
import team8.catan.rules.RuleChecker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class GameFactory {
    private final GameConfigLoader configLoader;
    private final RuleChecker ruleChecker;
    private final ActionLogger actionLogger;
    private final BaseMapLoader baseMapLoader;
    private final BoardFactory boardFactory;
    private final PathResolver pathResolver;

    public GameFactory() {
        this(
            new JsonLoader(),
            new RuleChecker(),
            new ConsoleActionLogger(),
            new BaseMapLoader(),
            new BoardFactory(),
            new PathResolver()
        );
    }

    public GameFactory(GameConfigLoader configLoader, RuleChecker ruleChecker) {
        this(configLoader, ruleChecker, new ConsoleActionLogger(), new BaseMapLoader(), new BoardFactory(), new PathResolver());
    }

    public GameFactory(GameConfigLoader configLoader, RuleChecker ruleChecker, ActionLogger actionLogger) {
        this(configLoader, ruleChecker, actionLogger, new BaseMapLoader(), new BoardFactory(), new PathResolver());
    }

    GameFactory(
        GameConfigLoader configLoader,
        RuleChecker ruleChecker,
        ActionLogger actionLogger,
        BaseMapLoader baseMapLoader,
        BoardFactory boardFactory,
        PathResolver pathResolver
    ) {
        this.configLoader = Objects.requireNonNull(configLoader, "configLoader");
        this.ruleChecker = Objects.requireNonNull(ruleChecker, "ruleChecker");
        this.actionLogger = Objects.requireNonNull(actionLogger, "actionLogger");
        this.baseMapLoader = Objects.requireNonNull(baseMapLoader, "baseMapLoader");
        this.boardFactory = Objects.requireNonNull(boardFactory, "boardFactory");
        this.pathResolver = Objects.requireNonNull(pathResolver, "pathResolver");
    }

    public Game createGame(Path configPath) throws IOException {
        GameConfig config = configLoader.load(configPath);
        Path configDirectory = configPath.getParent() == null ? Path.of(".") : configPath.getParent();
        return createGame(config, configDirectory);
    }

    public Game createGame(GameConfig config) {
        try {
            return createGame(config, Path.of("."));
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to build game from configuration", ex);
        }
    }

    private Game createGame(GameConfig config, Path configDirectory) throws IOException {
        Board board = buildBoard(config, configDirectory);
        HumanInputPort inputPort = null;
        if (config.getHumanPlayerIndex() != null) {
            inputPort = new ConsoleHumanInputPort(new Scanner(System.in), System.out);
        }

        StepForwardGate stepForwardGate = buildStepForwardGate(config.getHumanPlayerIndex(), inputPort);
        List<Player> players = buildPlayers(config, inputPort);
        seedStartingResources(players, config.getStartingResourcesPerType());
        GameStateWriter stateWriter = new JsonStateWriter(pathResolver.resolveOutputPath(configDirectory, config.getStatePath()));

        return new Game(
            board,
            players,
            ruleChecker,
            config.getMaxRounds(),
            config.getVictoryPointsToWin(),
            new TwoDice(),
            actionLogger,
            stepForwardGate,
            stateWriter
        );
    }

    private Board buildBoard(GameConfig config, Path configDirectory) throws IOException {
        Path baseMapPath = pathResolver.resolveInputPath(configDirectory, config.getBaseMapPath());
        List<BaseMapTileSpec> tileSpecs = baseMapLoader.load(baseMapPath, config.getBaseMapPath());
        return boardFactory.buildBoard(tileSpecs);
    }

    private StepForwardGate buildStepForwardGate(Integer humanPlayerIndex, HumanInputPort inputPort) {
        if (humanPlayerIndex == null) {
            return new NoOpStepForwardGate();
        }
        return new ConsoleStepForwardGate(inputPort);
    }

    private List<Player> buildPlayers(GameConfig config, HumanInputPort inputPort) {
        int numPlayers = config.getNumPlayers();
        Integer humanPlayerIndex = config.getHumanPlayerIndex();
        PlayerColor[] palette = PlayerColor.values();
        HumanCommandParser parser = new HumanCommandParser();

        List<Player> players = new ArrayList<>(numPlayers);
        for (int i = 0; i < numPlayers; i++) {
            PlayerColor color = palette[i % palette.length];
            if (humanPlayerIndex != null && i == humanPlayerIndex) {
                players.add(new HumanPlayer(i, color, inputPort, parser));
            } else {
                players.add(new RandomAgent(i, color));
            }
        }
        return players;
    }

    private void seedStartingResources(List<Player> players, int perResource) {
        for (Player player : players) {
            for (ResourceType type : ResourceType.values()) {
                player.grantResource(type, perResource);
            }
        }
    }
}
