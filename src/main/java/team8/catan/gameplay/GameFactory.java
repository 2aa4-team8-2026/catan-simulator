package team8.catan.gameplay;

import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.board.Tile;
import team8.catan.configuration.GameConfig;
import team8.catan.configuration.GameConfigLoader;
import team8.catan.configuration.JsonLoader;
import team8.catan.dice.TwoDice;
import team8.catan.output.ActionLogger;
import team8.catan.output.ConsoleActionLogger;
import team8.catan.output.GameStateWriter;
import team8.catan.output.JsonStateWriter;
import team8.catan.players.ConsoleHumanInputPort;
import team8.catan.players.HumanCommandParser;
import team8.catan.players.HumanInputPort;
import team8.catan.players.HumanPlayer;
import team8.catan.players.Player;
import team8.catan.players.PlayerColor;
import team8.catan.players.RandomAgent;
import team8.catan.rules.RuleChecker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GameFactory {
    private static final Pattern TILE_PATTERN = Pattern.compile(
        "\\{\\s*\"q\"\\s*:\\s*(-?\\d+)\\s*,\\s*\"s\"\\s*:\\s*(-?\\d+)\\s*,\\s*\"r\"\\s*:\\s*(-?\\d+)"
            + "\\s*,\\s*\"resource\"\\s*:\\s*(null|\"[^\"]+\")\\s*,\\s*\"number\"\\s*:\\s*(null|-?\\d+)\\s*\\}"
    );

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
        GameStateWriter stateWriter = new JsonStateWriter(resolveOutputPath(configDirectory, config.getStatePath()));

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
        Path baseMapPath = resolveInputPath(configDirectory, config.getBaseMapPath());
        List<TileSpec> tileSpecs = loadTileSpecs(baseMapPath, config.getBaseMapPath());

        List<LatticePoint> cornerOffsets = List.of(
            new LatticePoint(1, 1),
            new LatticePoint(0, 2),
            new LatticePoint(-1, 1),
            new LatticePoint(-1, -1),
            new LatticePoint(0, -2),
            new LatticePoint(1, -1)
        );

        Set<LatticePoint> vertices = new HashSet<>();
        Map<LatticePoint, Integer> tileIncidence = new HashMap<>();
        Map<LatticePoint, Set<LatticePoint>> adjacency = new HashMap<>();
        List<List<LatticePoint>> tileCornersById = new ArrayList<>(tileSpecs.size());

        for (TileSpec tileSpec : tileSpecs) {
            LatticePoint center = new LatticePoint(2 * tileSpec.q + tileSpec.r, 3 * tileSpec.r);
            List<LatticePoint> corners = new ArrayList<>(6);
            for (LatticePoint offset : cornerOffsets) {
                LatticePoint corner = center.add(offset);
                corners.add(corner);
                vertices.add(corner);
                tileIncidence.merge(corner, 1, Integer::sum);
                adjacency.computeIfAbsent(corner, ignored -> new HashSet<>());
            }
            tileCornersById.add(corners);

            for (int i = 0; i < corners.size(); i++) {
                LatticePoint a = corners.get(i);
                LatticePoint b = corners.get((i + 1) % corners.size());
                adjacency.get(a).add(b);
                adjacency.get(b).add(a);
            }
        }

        List<LatticePoint> centralRing = List.of(
            new LatticePoint(1, 1),   // 0
            new LatticePoint(1, -1),  // 1
            new LatticePoint(0, -2),  // 2
            new LatticePoint(-1, -1), // 3
            new LatticePoint(-1, 1),  // 4
            new LatticePoint(0, 2)    // 5
        );
        Set<LatticePoint> centralRingSet = new HashSet<>(centralRing);

        Set<LatticePoint> middleRingSet = new HashSet<>();
        Set<LatticePoint> outerRingSet = new HashSet<>();
        for (LatticePoint vertex : vertices) {
            int incidence = tileIncidence.getOrDefault(vertex, 0);
            if (incidence == 3 && !centralRingSet.contains(vertex)) {
                middleRingSet.add(vertex);
            } else if (incidence < 3) {
                outerRingSet.add(vertex);
            }
        }

        List<LatticePoint> middleRing = orderedClockwiseRing(middleRingSet, adjacency, new LatticePoint(2, -2));
        List<LatticePoint> outerRing = orderedClockwiseRing(outerRingSet, adjacency, new LatticePoint(4, -4));

        List<LatticePoint> orderedNodes = new ArrayList<>(54);
        orderedNodes.addAll(centralRing);
        orderedNodes.addAll(middleRing);
        orderedNodes.addAll(outerRing);

        if (orderedNodes.size() != 54) {
            throw new IllegalStateException("Expected 54 nodes, found " + orderedNodes.size());
        }

        Map<LatticePoint, Integer> nodeIds = new HashMap<>();
        List<Node> nodes = new ArrayList<>(54);
        for (int i = 0; i < orderedNodes.size(); i++) {
            nodeIds.put(orderedNodes.get(i), i);
            nodes.add(new Node(i));
        }

        List<int[]> edgeEndpoints = new ArrayList<>();
        for (Map.Entry<LatticePoint, Set<LatticePoint>> entry : adjacency.entrySet()) {
            LatticePoint a = entry.getKey();
            for (LatticePoint b : entry.getValue()) {
                if (comparePoints(a, b) < 0) {
                    Integer idA = nodeIds.get(a);
                    Integer idB = nodeIds.get(b);
                    if (idA == null || idB == null) {
                        throw new IllegalStateException("Missing node mapping for edge.");
                    }
                    int low = Math.min(idA, idB);
                    int high = Math.max(idA, idB);
                    edgeEndpoints.add(new int[] { low, high });
                }
            }
        }

        edgeEndpoints.sort(Comparator
            .comparingInt((int[] pair) -> pair[0])
            .thenComparingInt(pair -> pair[1]));

        if (edgeEndpoints.size() != 72) {
            throw new IllegalStateException("Expected 72 edges, found " + edgeEndpoints.size());
        }

        List<Edge> edges = new ArrayList<>(72);
        for (int i = 0; i < edgeEndpoints.size(); i++) {
            int[] pair = edgeEndpoints.get(i);
            edges.add(new Edge(i, pair[0], pair[1]));
        }

        List<Tile> tiles = new ArrayList<>(tileSpecs.size());
        int robberTileId = 0;
        for (int i = 0; i < tileSpecs.size(); i++) {
            TileSpec spec = tileSpecs.get(i);
            List<LatticePoint> corners = tileCornersById.get(i);
            int[] adjacentNodes = new int[corners.size()];
            for (int j = 0; j < corners.size(); j++) {
                Integer nodeId = nodeIds.get(corners.get(j));
                if (nodeId == null) {
                    throw new IllegalStateException("Missing node mapping for tile corner.");
                }
                adjacentNodes[j] = nodeId;
            }
            tiles.add(new Tile(i, spec.q, spec.s, spec.r, spec.resourceType, spec.numberToken, adjacentNodes));
            if (spec.resourceType == null) {
                robberTileId = i;
            }
        }

        return new Board(nodes, edges, tiles, robberTileId);
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

    private static List<LatticePoint> orderedClockwiseRing(
        Set<LatticePoint> ring,
        Map<LatticePoint, Set<LatticePoint>> adjacency,
        LatticePoint start
    ) {
        if (!ring.contains(start)) {
            throw new IllegalStateException("Ring does not contain expected start point: " + start);
        }

        Map<LatticePoint, List<LatticePoint>> ringNeighbors = new HashMap<>();
        for (LatticePoint point : ring) {
            List<LatticePoint> neighborsInRing = new ArrayList<>();
            for (LatticePoint neighbor : adjacency.getOrDefault(point, Set.of())) {
                if (ring.contains(neighbor)) {
                    neighborsInRing.add(neighbor);
                }
            }
            if (neighborsInRing.size() != 2) {
                throw new IllegalStateException("Expected ring degree 2 for " + point + ", found " + neighborsInRing.size());
            }
            ringNeighbors.put(point, neighborsInRing);
        }

        List<LatticePoint> startNeighbors = ringNeighbors.get(start);
        List<LatticePoint> optionA = traverseRing(ringNeighbors, start, startNeighbors.get(0), ring.size());
        List<LatticePoint> optionB = traverseRing(ringNeighbors, start, startNeighbors.get(1), ring.size());

        return signedArea(optionA) < 0 ? optionA : optionB;
    }

    private static List<LatticePoint> traverseRing(
        Map<LatticePoint, List<LatticePoint>> ringNeighbors,
        LatticePoint start,
        LatticePoint firstNeighbor,
        int expectedSize
    ) {
        List<LatticePoint> ordered = new ArrayList<>(expectedSize);
        ordered.add(start);

        LatticePoint previous = start;
        LatticePoint current = firstNeighbor;

        while (true) {
            if (current.equals(start)) {
                break;
            }
            ordered.add(current);
            if (ordered.size() > expectedSize) {
                throw new IllegalStateException("Ring traversal exceeded expected size");
            }

            List<LatticePoint> neighbors = ringNeighbors.get(current);
            LatticePoint next = neighbors.get(0).equals(previous) ? neighbors.get(1) : neighbors.get(0);
            previous = current;
            current = next;
        }

        if (ordered.size() != expectedSize) {
            throw new IllegalStateException("Ring traversal size mismatch: " + ordered.size() + " vs " + expectedSize);
        }

        return ordered;
    }

    private static double signedArea(List<LatticePoint> polygon) {
        double area = 0.0;
        for (int i = 0; i < polygon.size(); i++) {
            LatticePoint a = polygon.get(i);
            LatticePoint b = polygon.get((i + 1) % polygon.size());
            area += (double) a.x * b.y - (double) b.x * a.y;
        }
        return area;
    }

    private static int comparePoints(LatticePoint a, LatticePoint b) {
        int byX = Integer.compare(a.x, b.x);
        if (byX != 0) {
            return byX;
        }
        return Integer.compare(a.y, b.y);
    }

    private static Path resolveInputPath(Path configDirectory, String configuredPath) {
        Path rawPath = Path.of(configuredPath);
        if (rawPath.isAbsolute()) {
            return rawPath.normalize();
        }

        Path fromConfigDir = configDirectory.resolve(rawPath).normalize();
        if (Files.exists(fromConfigDir)) {
            return fromConfigDir;
        }

        Path fromWorkingDir = rawPath.normalize();
        if (Files.exists(fromWorkingDir)) {
            return fromWorkingDir;
        }

        return fromConfigDir;
    }

    private static Path resolveOutputPath(Path configDirectory, String configuredPath) {
        Path rawPath = Path.of(configuredPath);
        if (rawPath.isAbsolute()) {
            return rawPath.normalize();
        }

        if (Files.isDirectory(configDirectory)) {
            return configDirectory.resolve(rawPath).normalize();
        }

        return rawPath.normalize();
    }

    private static List<TileSpec> loadTileSpecs(Path resolvedPath, String configuredPath) throws IOException {
        String json = loadText(resolvedPath, configuredPath);
        Matcher matcher = TILE_PATTERN.matcher(json);
        List<TileSpec> specs = new ArrayList<>();

        while (matcher.find()) {
            int q = Integer.parseInt(matcher.group(1));
            int s = Integer.parseInt(matcher.group(2));
            int r = Integer.parseInt(matcher.group(3));
            ResourceType resourceType = parseResourceType(matcher.group(4));
            Integer numberToken = "null".equals(matcher.group(5)) ? null : Integer.parseInt(matcher.group(5));
            specs.add(new TileSpec(q, s, r, resourceType, numberToken));
        }

        if (specs.size() != 19) {
            throw new IllegalArgumentException(
                "Expected 19 tiles in base map (" + resolvedPath + "), found " + specs.size()
            );
        }

        return specs;
    }

    private static String loadText(Path resolvedPath, String configuredPath) throws IOException {
        if (Files.exists(resolvedPath)) {
            return Files.readString(resolvedPath);
        }

        String configuredResourcePath = configuredPath.replace('\\', '/');
        InputStream configuredResource = GameFactory.class.getClassLoader().getResourceAsStream(configuredResourcePath);
        if (configuredResource != null) {
            try (InputStream in = configuredResource) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        String resolvedResourcePath = resolvedPath.toString().replace('\\', '/');
        InputStream resolvedResource = GameFactory.class.getClassLoader().getResourceAsStream(resolvedResourcePath);
        if (resolvedResource != null) {
            try (InputStream in = resolvedResource) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        throw new java.nio.file.NoSuchFileException(
            "Could not locate configured map file at '" + resolvedPath + "' or classpath resource '" + configuredPath + "'"
        );
    }

    private static ResourceType parseResourceType(String rawToken) {
        if ("null".equals(rawToken)) {
            return null;
        }

        String value = rawToken.substring(1, rawToken.length() - 1).toUpperCase();
        return switch (value) {
            case "WOOD", "LUMBER" -> ResourceType.LUMBER;
            case "WHEAT", "GRAIN" -> ResourceType.GRAIN;
            case "SHEEP", "WOOL" -> ResourceType.WOOL;
            case "BRICK" -> ResourceType.BRICK;
            case "ORE" -> ResourceType.ORE;
            default -> throw new IllegalArgumentException("Unknown tile resource: " + value);
        };
    }

    private static final class LatticePoint {
        private final int x;
        private final int y;

        private LatticePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private LatticePoint add(LatticePoint other) {
            return new LatticePoint(x + other.x, y + other.y);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LatticePoint)) {
                return false;
            }
            LatticePoint other = (LatticePoint) obj;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private static final class TileSpec {
        private final int q;
        private final int s;
        private final int r;
        private final ResourceType resourceType;
        private final Integer numberToken;

        private TileSpec(int q, int s, int r, ResourceType resourceType, Integer numberToken) {
            this.q = q;
            this.s = s;
            this.r = r;
            this.resourceType = resourceType;
            this.numberToken = numberToken;
        }
    }

    private void seedStartingResources(List<Player> players, int perResource) {
        for (Player player : players) {
            for (ResourceType type : ResourceType.values()) {
                player.getResourceHand().add(type, perResource);
            }
        }
    }
}
