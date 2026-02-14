package configuration;

// todo: add javadoc
public final class GameConfig {
    private static final int MAX_ALLOWED_ROUNDS = 8192;

    private final int numPlayers;
    private final int maxRounds;
    private final int victoryPointsToWin;
    private final int startingResourcesPerType;

    public GameConfig(
        int numPlayers,
        int maxRounds,
        int victoryPointsToWin,
        int startingResourcesPerType
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

        this.numPlayers = numPlayers;
        this.maxRounds = maxRounds;
        this.victoryPointsToWin = victoryPointsToWin;
        this.startingResourcesPerType = startingResourcesPerType;
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

}
