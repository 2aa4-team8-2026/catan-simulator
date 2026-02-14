package team8.catan.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// todo: add javadoc
public final class JsonLoader extends GameConfigLoader {
    private static final Pattern INTEGER_FIELD_PATTERN =
        Pattern.compile("\"([^\"]+)\"\\s*:\\s*(-?\\d+)");

    @Override
    public GameConfig load(Path path) throws IOException {
        String json = Files.readString(path);

        Integer numPlayers = null;
        Integer maxRounds = null;
        Integer victoryPointsToWin = null;
        Integer startingResourcesPerType = null;

        Matcher matcher = INTEGER_FIELD_PATTERN.matcher(json);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            int value = Integer.parseInt(matcher.group(2));

            switch (fieldName) {
                case "numPlayers":
                    numPlayers = value;
                    break;
                case "turns":
                case "maxRounds":
                    maxRounds = value;
                    break;
                case "victoryPointsToWin":
                    victoryPointsToWin = value;
                    break;
                case "startingResourcesPerType":
                    startingResourcesPerType = value;
                    break;
                default:
                    // Ignore unrelated config keys.
                    break;
            }
        }

        validateRequiredField("numPlayers", numPlayers);
        validateRequiredField("turns/maxRounds", maxRounds);
        validateRequiredField("victoryPointsToWin", victoryPointsToWin);
        validateRequiredField("startingResourcesPerType", startingResourcesPerType);

        return new GameConfig(
            numPlayers,
            maxRounds,
            victoryPointsToWin,
            startingResourcesPerType
        );
    }

    private static void validateRequiredField(String fieldName, Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing required config field: " + fieldName);
        }
    }
}
