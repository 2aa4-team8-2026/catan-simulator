package team8.catan.players;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HumanCommandParser {
    private static final Pattern SETTLEMENT_PATTERN = Pattern.compile("(?i)^build\\s+settlement\\s+(\\d+)\\s*$");
    private static final Pattern CITY_PATTERN = Pattern.compile("(?i)^build\\s+city\\s+(\\d+)\\s*$");
    private static final Pattern ROAD_PATTERN = Pattern.compile("(?i)^build\\s+road\\s+(\\d+)\\s*,\\s*(\\d+)\\s*$");
    private static final Pattern ROAD_PATTERN_SPACED = Pattern.compile("(?i)^build\\s+road\\s+(\\d+)\\s+(\\d+)\\s*$");
    private static final Pattern SHORT_SETTLEMENT_PATTERN = Pattern.compile("(?i)^b\\s+s(?:ettlement)?\\s+(\\d+)\\s*$");
    private static final Pattern SHORT_CITY_PATTERN = Pattern.compile("(?i)^b\\s+c(?:ity)?\\s+(\\d+)\\s*$");
    private static final Pattern SHORT_ROAD_PATTERN = Pattern.compile("(?i)^b\\s+r(?:oad)?\\s+(\\d+)\\s*,\\s*(\\d+)\\s*$");
    private static final Pattern SHORT_ROAD_PATTERN_SPACED = Pattern.compile("(?i)^b\\s+r(?:oad)?\\s+(\\d+)\\s+(\\d+)\\s*$");

    public HumanCommand parse(String rawInput) {
        if (rawInput == null) {
            return HumanCommand.invalid("Command cannot be null");
        }

        String input = rawInput.trim();
        if (input.isEmpty()) {
            return HumanCommand.go();
        }

        if (input.equalsIgnoreCase("roll") || input.equalsIgnoreCase("r")) {
            return HumanCommand.roll();
        }
        if (input.equalsIgnoreCase("go") || input.equalsIgnoreCase("g")) {
            return HumanCommand.go();
        }
        if (input.equalsIgnoreCase("list") || input.equalsIgnoreCase("ls")) {
            return HumanCommand.list();
        }
        if (input.equalsIgnoreCase("actions")
            || input.equalsIgnoreCase("a")
            || input.equalsIgnoreCase("help")
            || input.equals("?")) {
            return HumanCommand.showActions();
        }
        if (input.equalsIgnoreCase("build") || input.equalsIgnoreCase("b")) {
            return HumanCommand.buildMenu();
        }

        HumanCommand directBuild = parseBuildCommand(input);
        if (directBuild != null) {
            return directBuild;
        }

        return HumanCommand.invalid("Unknown command. Use r, b, ls, Enter(go), or a.");
    }

    private static HumanCommand parseBuildCommand(String input) {
        Matcher settlementMatcher = SETTLEMENT_PATTERN.matcher(input);
        if (settlementMatcher.matches()) {
            return HumanCommand.buildSettlement(Integer.parseInt(settlementMatcher.group(1)));
        }

        Matcher cityMatcher = CITY_PATTERN.matcher(input);
        if (cityMatcher.matches()) {
            return HumanCommand.buildCity(Integer.parseInt(cityMatcher.group(1)));
        }

        Matcher roadMatcher = ROAD_PATTERN.matcher(input);
        if (roadMatcher.matches()) {
            return HumanCommand.buildRoad(
                Integer.parseInt(roadMatcher.group(1)),
                Integer.parseInt(roadMatcher.group(2))
            );
        }

        Matcher roadMatcherSpaced = ROAD_PATTERN_SPACED.matcher(input);
        if (roadMatcherSpaced.matches()) {
            return HumanCommand.buildRoad(
                Integer.parseInt(roadMatcherSpaced.group(1)),
                Integer.parseInt(roadMatcherSpaced.group(2))
            );
        }

        Matcher shortSettlementMatcher = SHORT_SETTLEMENT_PATTERN.matcher(input);
        if (shortSettlementMatcher.matches()) {
            return HumanCommand.buildSettlement(Integer.parseInt(shortSettlementMatcher.group(1)));
        }

        Matcher shortCityMatcher = SHORT_CITY_PATTERN.matcher(input);
        if (shortCityMatcher.matches()) {
            return HumanCommand.buildCity(Integer.parseInt(shortCityMatcher.group(1)));
        }

        Matcher shortRoadMatcher = SHORT_ROAD_PATTERN.matcher(input);
        if (shortRoadMatcher.matches()) {
            return HumanCommand.buildRoad(
                Integer.parseInt(shortRoadMatcher.group(1)),
                Integer.parseInt(shortRoadMatcher.group(2))
            );
        }

        Matcher shortRoadMatcherSpaced = SHORT_ROAD_PATTERN_SPACED.matcher(input);
        if (shortRoadMatcherSpaced.matches()) {
            return HumanCommand.buildRoad(
                Integer.parseInt(shortRoadMatcherSpaced.group(1)),
                Integer.parseInt(shortRoadMatcherSpaced.group(2))
            );
        }

        return null;
    }
}
