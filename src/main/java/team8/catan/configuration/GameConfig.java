package team8.catan.configuration;

// todo: add javadoc
public final class GameConfig {
    private static final int MAX_ALLOWED_ROUNDS = 8192;
    private static final String DEFAULT_BASE_MAP_PATH = "base_map.json";
    private static final String DEFAULT_STATE_PATH = "state.json";

    private final int numPlayers;
    private final int maxRounds;
    private final int victoryPointsToWin;
    private final int startingResourcesPerType;
    private final Integer humanPlayerIndex;
    private final String baseMapPath;
    private final String statePath;

    public GameConfig(
        int numPlayers,
        int maxRounds,
        int victoryPointsToWin,
        int startingResourcesPerType
    ) {
        this(
            numPlayers,
            maxRounds,
            victoryPointsToWin,
            startingResourcesPerType,
            null,
            DEFAULT_BASE_MAP_PATH,
            DEFAULT_STATE_PATH
        );
    }

    public GameConfig(
        int numPlayers,
        int maxRounds,
        int victoryPointsToWin,
        int startingResourcesPerType,
        Integer humanPlayerIndex
    ) {
        this(
            numPlayers,
            maxRounds,
            victoryPointsToWin,
            startingResourcesPerType,
            humanPlayerIndex,
            DEFAULT_BASE_MAP_PATH,
            DEFAULT_STATE_PATH
        );
    }

    public GameConfig(
        int numPlayers,
        int maxRounds,
        int victoryPointsToWin,
        int startingResourcesPerType,
        Integer humanPlayerIndex,
        String baseMapPath,
        String statePath
    ) {
        if (numPlayers <= 0) {
            throw new IllegalArgumentException("numPlayers must be > 0");
        }
        if (maxRounds <= 0) {
            throw new IllegalArgumentException("maxRounds must be > 0");
        }
        if (maxRounds > MAX_ALLOWED_ROUNDS) {
            throw new IllegalArgumentException("maxRounds must be <= " + MAX_ALLOWED_ROUNDS);
        }
        if (victoryPointsToWin <= 0) {
            throw new IllegalArgumentException("victoryPointsToWin must be > 0");
        }
        if (startingResourcesPerType < 0) {
            throw new IllegalArgumentException("startingResourcesPerType must be >= 0");
        }
        if (humanPlayerIndex != null && (humanPlayerIndex < 0 || humanPlayerIndex >= numPlayers)) {
            throw new IllegalArgumentException("humanPlayerIndex must be in [0, numPlayers)");
        }
        if (baseMapPath == null || baseMapPath.isBlank()) {
            throw new IllegalArgumentException("baseMapPath must be provided");
        }
        if (statePath == null || statePath.isBlank()) {
            throw new IllegalArgumentException("statePath must be provided");
        }

        this.numPlayers = numPlayers;
        this.maxRounds = maxRounds;
        this.victoryPointsToWin = victoryPointsToWin;
        this.startingResourcesPerType = startingResourcesPerType;
        this.humanPlayerIndex = humanPlayerIndex;
        this.baseMapPath = baseMapPath;
        this.statePath = statePath;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public int getVictoryPointsToWin() {
        return victoryPointsToWin;
    }

    public int getStartingResourcesPerType() {
        return startingResourcesPerType;
    }

    public Integer getHumanPlayerIndex() {
        return humanPlayerIndex;
    }

    public String getBaseMapPath() {
        return baseMapPath;
    }

    public String getStatePath() {
        return statePath;
    }

}
