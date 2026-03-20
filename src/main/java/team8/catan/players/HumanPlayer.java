package team8.catan.players;

import team8.catan.actions.Action;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.ResourceType;
import team8.catan.gameplay.GamePhase;
import team8.catan.rules.RuleChecker;

import java.util.ArrayList;
import java.util.List;

public class HumanPlayer extends Player {
    private final HumanInputPort inputPort;
    private final HumanCommandParser commandParser;

    public HumanPlayer(int id, PlayerColor color, HumanInputPort inputPort, HumanCommandParser commandParser) {
        super(id, color);
        this.inputPort = inputPort;
        this.commandParser = commandParser;
    }

    @Override
    public Action chooseAction(Board board, RuleChecker ruleChecker, GamePhase phase) {
        while (true) {
            printPossibleActions(board, ruleChecker, phase);
            HumanCommand command = commandParser.parse(
                inputPort.readLine(buildPrompt(phase))
            );
            Action action = command.executeAction(this, board, ruleChecker, phase);
            if (action != null) {
                return action;
            }
        }
    }

    Action promptForBuildAction(Board board, RuleChecker ruleChecker, GamePhase phase) {
        ActionType type = chooseBuildType(phase);
        if (type == null) {
            return null;
        }

        printPossibleTargetsForActionType(board, ruleChecker, phase, type);

        while (true) {
            Action candidate = readBuildTarget(type, board, phase);
            if (candidate == null) {
                return null;
            }

            if (ruleChecker.isLegal(candidate, board, this, phase)) {
                return candidate;
            }

            printLine("Invalid target for " + describeBuildType(type) + ". Check legal targets and try again.");
            printPossibleTargetsForActionType(board, ruleChecker, phase, type);
        }
    }

    private ActionType chooseBuildType(GamePhase phase) {
        if (phase == GamePhase.SETUP_SETTLEMENT) {
            return ActionType.BUILD_SETTLEMENT;
        }
        if (phase == GamePhase.SETUP_ROAD) {
            return ActionType.BUILD_ROAD;
        }

        while (true) {
            String input = inputPort.readLine("Build type [s=settlement, c=city, r=road, Enter=cancel]: ");
            ActionType parsed = commandParser.parseBuildActionType(input);
            if (parsed != null) {
                return parsed;
            }
            if (input == null || input.trim().isEmpty()) {
                return null;
            }
            inputPort.printLine("Unknown build type. Use s, c, or r.");
        }
    }

    private Action readBuildTarget(ActionType type, Board board, GamePhase phase) {
        if (type == ActionType.BUILD_ROAD) {
            while (true) {
                String raw = inputPort.readLine(roadPrompt(phase));
                if (raw == null || raw.trim().isEmpty()) {
                    if (isMandatorySetupPhase(phase)) {
                        inputPort.printLine("Setup road placement is mandatory. Enter legal road endpoints.");
                        continue;
                    }
                    return null;
                }

                int[] endpoints = parseRoadEndpoints(raw);
                if (endpoints == null) {
                    continue;
                }

                int edgeId = board.getEdgeIdBetweenNodes(endpoints[0], endpoints[1]);
                if (edgeId < 0) {
                    inputPort.printLine("No edge exists between nodes " + endpoints[0] + " and " + endpoints[1] + ".");
                    continue;
                }
                return new Action(ActionType.BUILD_ROAD, edgeId);
            }
        }

        while (true) {
            String raw = inputPort.readLine(nodePrompt(type, phase));
            if (raw == null || raw.trim().isEmpty()) {
                if (isMandatorySetupPhase(phase)) {
                    inputPort.printLine("Setup " + describeBuildType(type).toLowerCase() + " placement is mandatory.");
                    continue;
                }
                return null;
            }
            try {
                return new Action(type, Integer.parseInt(raw.trim()));
            } catch (NumberFormatException ex) {
                inputPort.printLine("Node id must be an integer.");
            }
        }
    }

    Action actionFromBuildCommand(HumanCommand command, Board board) {
        switch (command.getType()) {
            case BUILD_SETTLEMENT:
                if (command.getNodeId() == null) {
                    printLine("Settlement command missing node id.");
                    return null;
                }
                return new Action(ActionType.BUILD_SETTLEMENT, command.getNodeId());
            case BUILD_CITY:
                if (command.getNodeId() == null) {
                    printLine("City command missing node id.");
                    return null;
                }
                return new Action(ActionType.BUILD_CITY, command.getNodeId());
            case BUILD_ROAD:
                if (command.getFromNodeId() == null || command.getToNodeId() == null) {
                    printLine("Road command missing node ids.");
                    return null;
                }
                int edgeId = board.getEdgeIdBetweenNodes(command.getFromNodeId(), command.getToNodeId());
                if (edgeId < 0) {
                    printLine(
                        "No edge exists between nodes " + command.getFromNodeId() + " and " + command.getToNodeId() + "."
                    );
                    return null;
                }
                return new Action(ActionType.BUILD_ROAD, edgeId);
            default:
                return null;
        }
    }

    private int[] parseRoadEndpoints(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        String normalized = trimmed.replace(",", " ");
        String[] parts = normalized.trim().split("\\s+");
        if (parts.length != 2) {
            inputPort.printLine("Enter exactly 2 node ids, e.g. 12,19");
            return null;
        }

        try {
            return new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) };
        } catch (NumberFormatException ex) {
            inputPort.printLine("Node ids must be integers.");
            return null;
        }
    }

    void printPossibleActions(Board board, RuleChecker ruleChecker, GamePhase phase) {
        List<Action> legalActions = ruleChecker.getLegalActions(board, this, phase);
        if (isMandatorySetupPhase(phase)) {
            printLine("Possible actions now: ls=list, undo, redo, b=build.");
        } else {
            printLine("Possible actions now: Enter=pass, ls=list, undo, redo, b=build.");
        }
        boolean hasBuildAction = false;
        for (Action action : legalActions) {
            if (isBuildTypeAllowedInPhase(action.getActionType(), phase)) {
                hasBuildAction = true;
                break;
            }
        }
        if (!hasBuildAction) {
            printLine("No legal build actions available.");
        }
    }

    void printPossibleTargetsForActionType(
        Board board,
        RuleChecker ruleChecker,
        GamePhase phase,
        ActionType actionType
    ) {
        if (!isBuildTypeAllowedInPhase(actionType, phase)) {
            return;
        }

        List<Action> legalActions = ruleChecker.getLegalActions(board, this, phase);
        if (actionType == ActionType.BUILD_ROAD) {
            List<String> roadPairs = new ArrayList<>();
            for (Action legalAction : legalActions) {
                if (legalAction.getActionType() != ActionType.BUILD_ROAD) {
                    continue;
                }
                Edge edge = board.getEdge(legalAction.getTargetId());
                if (edge != null) {
                    roadPairs.add(edge.getNodeA() + "-" + edge.getNodeB());
                }
            }
            if (roadPairs.isEmpty()) {
                printLine("No legal road targets available.");
            } else {
                printLine("Road endpoints: " + summarizeList(roadPairs));
            }
            return;
        }

        List<Integer> nodeTargets = new ArrayList<>();
        for (Action legalAction : legalActions) {
            if (legalAction.getActionType() == actionType) {
                nodeTargets.add(legalAction.getTargetId());
            }
        }

        if (nodeTargets.isEmpty()) {
            printLine("No legal " + describeBuildType(actionType).toLowerCase() + " targets available.");
            return;
        }

        printLine(describeBuildType(actionType) + " nodes: " + summarizeList(nodeTargets));
    }

    private static String summarizeList(List<?> values) {
        int limit = 20;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size() && i < limit; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(values.get(i));
        }
        if (values.size() > limit) {
            sb.append(" ... (+").append(values.size() - limit).append(")");
        }
        return sb.toString();
    }

    static boolean isBuildTypeAllowedInPhase(ActionType type, GamePhase phase) {
        if (phase == GamePhase.SETUP_SETTLEMENT) {
            return type == ActionType.BUILD_SETTLEMENT;
        }
        if (phase == GamePhase.SETUP_ROAD) {
            return type == ActionType.BUILD_ROAD;
        }
        return type == ActionType.BUILD_SETTLEMENT
            || type == ActionType.BUILD_CITY
            || type == ActionType.BUILD_ROAD;
    }

    static boolean isMandatorySetupPhase(GamePhase phase) {
        return phase == GamePhase.SETUP_SETTLEMENT || phase == GamePhase.SETUP_ROAD;
    }

    private String buildPrompt(GamePhase phase) {
        if (isMandatorySetupPhase(phase)) {
            return "P" + getId() + " action [b/ls]: ";
        }
        return "P" + getId() + " action [b/ls/Enter=go]: ";
    }

    private String roadPrompt(GamePhase phase) {
        if (isMandatorySetupPhase(phase)) {
            return "Road endpoints [from,to] or [from to]: ";
        }
        return "Road endpoints [from,to] or [from to] (Enter=cancel): ";
    }

    private String nodePrompt(ActionType type, GamePhase phase) {
        if (isMandatorySetupPhase(phase)) {
            return describeBuildType(type) + " node id: ";
        }
        return describeBuildType(type) + " node id (Enter=cancel): ";
    }

    private static String describeBuildType(ActionType type) {
        switch (type) {
            case BUILD_SETTLEMENT:
                return "Settlement";
            case BUILD_CITY:
                return "City";
            case BUILD_ROAD:
                return "Road";
            default:
                return "Build";
        }
    }

    private String resourceSummary() {
        StringBuilder sb = new StringBuilder("Cards: ");
        for (ResourceType type : ResourceType.values()) {
            if (sb.length() > "Cards: ".length()) {
                sb.append(", ");
            }
            sb.append(type.name())
                .append("=")
                .append(getResourceCount(type));
        }
        sb.append(" (total=").append(getTotalResourceCards()).append(")");
        return sb.toString();
    }

    void printResourceSummary() {
        printLine(resourceSummary());
    }

    void printLine(String message) {
        inputPort.printLine(message);
    }
}
