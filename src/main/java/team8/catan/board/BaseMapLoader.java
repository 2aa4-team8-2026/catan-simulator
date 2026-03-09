package team8.catan.board;

import team8.catan.io.JsonParser;
import team8.catan.io.TextResourceLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseMapLoader {
    public List<BaseMapTileSpec> load(Path resolvedPath, String configuredPath) throws IOException {
        String json = TextResourceLoader.load(resolvedPath, configuredPath, BaseMapLoader.class);
        Map<String, Object> root = JsonParser.parseObject(json);
        Object tilesValue = root.get("tiles");
        if (!(tilesValue instanceof List<?> tiles)) {
            throw new IllegalArgumentException("Base map must contain a 'tiles' array");
        }

        List<BaseMapTileSpec> specs = new ArrayList<>(tiles.size());
        for (Object tileValue : tiles) {
            if (!(tileValue instanceof Map<?, ?> rawTileMap)) {
                throw new IllegalArgumentException("Each tile entry must be an object");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> tileMap = (Map<String, Object>) rawTileMap;
            specs.add(new BaseMapTileSpec(
                readInt(tileMap, "q"),
                readInt(tileMap, "s"),
                readInt(tileMap, "r"),
                parseResourceType(tileMap.get("resource")),
                readNullableInt(tileMap, "number")
            ));
        }

        if (specs.size() != 19) {
            throw new IllegalArgumentException(
                "Expected 19 tiles in base map (" + resolvedPath + "), found " + specs.size()
            );
        }
        return specs;
    }

    private static int readInt(Map<String, Object> tileMap, String key) {
        Object value = tileMap.get(key);
        if (!(value instanceof Number numberValue)) {
            throw new IllegalArgumentException("Tile field must be an integer: " + key);
        }
        long asLong = numberValue.longValue();
        if (numberValue.doubleValue() != (double) asLong) {
            throw new IllegalArgumentException("Tile field must be an integer: " + key);
        }
        return (int) asLong;
    }

    private static Integer readNullableInt(Map<String, Object> tileMap, String key) {
        Object value = tileMap.get(key);
        if (value == null) {
            return null;
        }
        return readInt(tileMap, key);
    }

    private static ResourceType parseResourceType(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (!(rawValue instanceof String stringValue)) {
            throw new IllegalArgumentException("Tile resource must be a string or null");
        }

        String value = stringValue.toUpperCase();
        return switch (value) {
            case "WOOD", "LUMBER" -> ResourceType.LUMBER;
            case "WHEAT", "GRAIN" -> ResourceType.GRAIN;
            case "SHEEP", "WOOL" -> ResourceType.WOOL;
            case "BRICK" -> ResourceType.BRICK;
            case "ORE" -> ResourceType.ORE;
            default -> throw new IllegalArgumentException("Unknown tile resource: " + value);
        };
    }
}
