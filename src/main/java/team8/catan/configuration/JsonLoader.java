package team8.catan.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// todo: add javadoc
public final class JsonLoader extends GameConfigLoader {
    private static final Pattern INTEGER_FIELD_PATTERN =
        Pattern.compile("\"([^\"]+)\"\\s*:\\s*(-?\\d+)");
    private static final Pattern STRING_FIELD_PATTERN =
        Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");

    @Override
    public GameConfig load(Path path) throws IOException {
        String json = loadJson(path);

        Integer numPlayers = null;
        Integer maxRounds = null;
        Integer victoryPointsToWin = null;
        Integer startingResourcesPerType = null;
        Integer humanPlayerIndex = null;
        String baseMapPath = null;
        String statePath = null;

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
                case "humanPlayerIndex":
                    humanPlayerIndex = value;
                    break;
                default:
                    // Ignore unrelated config keys.
                    break;
            }
        }

        Matcher stringMatcher = STRING_FIELD_PATTERN.matcher(json);
        while (stringMatcher.find()) {
            String fieldName = stringMatcher.group(1);
            String value = stringMatcher.group(2);
            switch (fieldName) {
                case "baseMapPath":
                    baseMapPath = value;
                    break;
                case "statePath":
                    statePath = value;
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
            startingResourcesPerType,
            humanPlayerIndex,
            baseMapPath == null ? "base_map.json" : baseMapPath,
            statePath == null ? "state.json" : statePath
        );
    }

    private static String loadJson(Path path) throws IOException {
        if (Files.exists(path)) {
            return Files.readString(path);
        }

        String resourcePath = path.toString().replace('\\', '/');
        InputStream resource = JsonLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resource == null) {
            throw new java.nio.file.NoSuchFileException(path.toString());
        }

        try (InputStream in = resource) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void validateRequiredField(String fieldName, Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing required config field: " + fieldName);
        }
    }
}
