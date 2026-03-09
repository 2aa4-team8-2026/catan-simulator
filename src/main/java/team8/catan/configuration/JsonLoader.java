package team8.catan.configuration;

import team8.catan.io.JsonParser;
import team8.catan.io.TextResourceLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

// todo: add javadoc
public class JsonLoader extends GameConfigLoader {
    @Override
    public GameConfig load(Path path) throws IOException {
        String json = TextResourceLoader.load(path, path.toString(), JsonLoader.class);
        Map<String, Object> root = JsonParser.parseObject(json);

        Integer numPlayers = readRequiredInt(root, "numPlayers");
        Integer maxRounds = readOptionalInt(root, "maxRounds");
        if (maxRounds == null) {
            maxRounds = readRequiredInt(root, "turns");
        }
        Integer victoryPointsToWin = readRequiredInt(root, "victoryPointsToWin");
        Integer startingResourcesPerType = readRequiredInt(root, "startingResourcesPerType");
        Integer humanPlayerIndex = readOptionalInt(root, "humanPlayerIndex");
        String baseMapPath = readOptionalString(root, "baseMapPath");
        String statePath = readOptionalString(root, "statePath");

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

    private static Integer readRequiredInt(Map<String, Object> root, String key) {
        Object value = root.get(key);
        if (value == null) {
            return null;
        }
        return toInt(key, value);
    }

    private static Integer readOptionalInt(Map<String, Object> root, String key) {
        Object value = root.get(key);
        if (value == null) {
            return null;
        }
        return toInt(key, value);
    }

    private static String readOptionalString(Map<String, Object> root, String key) {
        Object value = root.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String stringValue)) {
            throw new IllegalArgumentException("Expected string config field: " + key);
        }
        return stringValue;
    }

    private static Integer toInt(String key, Object value) {
        if (!(value instanceof Number numberValue)) {
            throw new IllegalArgumentException("Expected integer config field: " + key);
        }
        long asLong = numberValue.longValue();
        if (asLong < Integer.MIN_VALUE || asLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Config field out of integer range: " + key);
        }
        if (numberValue.doubleValue() != (double) asLong) {
            throw new IllegalArgumentException("Expected integer config field: " + key);
        }
        return (int) asLong;
    }

    private static void validateRequiredField(String fieldName, Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing required config field: " + fieldName);
        }
    }
}
