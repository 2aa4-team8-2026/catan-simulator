package team8.catan.board;

import team8.catan.io.TextResourceLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseMapLoader {
    private static final Pattern TILE_PATTERN = Pattern.compile(
        "\\{\\s*\"q\"\\s*:\\s*(-?\\d+)\\s*,\\s*\"s\"\\s*:\\s*(-?\\d+)\\s*,\\s*\"r\"\\s*:\\s*(-?\\d+)"
            + "\\s*,\\s*\"resource\"\\s*:\\s*(null|\"[^\"]+\")\\s*,\\s*\"number\"\\s*:\\s*(null|-?\\d+)\\s*\\}"
    );

    public List<BaseMapTileSpec> load(Path resolvedPath, String configuredPath) throws IOException {
        String json = TextResourceLoader.load(resolvedPath, configuredPath, BaseMapLoader.class);
        Matcher matcher = TILE_PATTERN.matcher(json);
        List<BaseMapTileSpec> specs = new ArrayList<>();
        while (matcher.find()) {
            specs.add(new BaseMapTileSpec(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                parseResourceType(matcher.group(4)),
                "null".equals(matcher.group(5)) ? null : Integer.parseInt(matcher.group(5))
            ));
        }

        if (specs.size() != 19) {
            throw new IllegalArgumentException(
                "Expected 19 tiles in base map (" + resolvedPath + "), found " + specs.size()
            );
        }
        return specs;
    }

    private static ResourceType parseResourceType(String rawValue) {
        if ("null".equals(rawValue)) {
            return null;
        }
        String value = rawValue.substring(1, rawValue.length() - 1).toUpperCase();
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
