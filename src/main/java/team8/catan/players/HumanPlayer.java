package team8.catan.players;

import team8.catan.actions.Action;
import team8.catan.actions.ActionTarget;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.ResourceType;
import team8.catan.gameplay.GamePhase;
import team8.catan.rules.RuleChecker;

import java.util.ArrayList;
import java.util.List;

public final class HumanPlayer extends Player {
    private final HumanInputPort inputPort;
    private final HumanCommandParser commandParser;
    private boolean rolledThisTurn;

    public HumanPlayer(int id, PlayerColor color, HumanInputPort inputPort, HumanCommandParser commandParser) {
        super(id, color);
        this.inputPort = inputPort;
        this.commandParser = commandParser;
        this.rolledThisTurn = false;
    }

    public void beginTurn() {
        rolledThisTurn = false;
    }

    public void awaitRollCommand() {
        inputPort.printLine("P" + getId() + " roll stage: use r=roll, ls=list cards.");
        while (!rolledThisTurn) {
            HumanCommand command = commandParser.parse(inputPort.readLine("P" + getId() + " roll> "));
            switch (command.getType()) {
                case ROLL:
                    rolledThisTurn = true;
                    break;
                case LIST:
                    inputPort.printLine(resourceSummary());
                    break;
                case SHOW_ACTIONS:
                    inputPort.printLine("Roll stage actions: r=roll, ls=list cards.");
                    break;
                case INVALID:
                    inputPort.printLine(command.getError());
                    break;
                default:
                    inputPort.printLine("You must roll first. Use r.");
                    break;
            }
        }
    }

    @Override
    public Action chooseAction(Board board, RuleChecker ruleChecker, GamePhase phase) {
        printPossibleActions(board, ruleChecker, phase);
        while (true) {
            HumanCommand command = commandParser.parse(
                inputPort.readLine("P" + getId() + " action [b/ls/a/Enter=go]: ")
            );
            switch (command.getType()) {
                case LIST:
                    inputPort.printLine(resourceSummary());
                    continue;
                case SHOW_ACTIONS:
                    printPossibleActions(board, ruleChecker, phase);
                    continue;
                case GO:
                    return new Action(ActionType.PASS, ActionTarget.NO_TARGET_ID);
                case ROLL:
                    inputPort.printLine("Roll is handled before this step. Use b, ls, a, or Enter.");
                    continue;
                case BUILD_MENU: {
                    Action buildAction = promptForBuildAction(board, ruleChecker, phase);
                    if (buildAction != null) {
                        return buildAction;
                    }
                    continue;
                }
                case BUILD_SETTLEMENT:
                case BUILD_CITY:
                case BUILD_ROAD: {
                    Action action = actionFromBuildCommand(command, board);
                    if (action == null) {
                        continue;
                    }
                    if (!isBuildTypeAllowedInPhase(action.getActionType(), phase)) {
                        inputPort.printLine("That build type is not available in this phase.");
                        continue;
                    }
                    if (!ruleChecker.isLegal(action, board, this, phase)) {
                        inputPort.printLine("That build is not legal right now.");
                        printPossibleTargetsForActionType(board, ruleChecker, phase, action.getActionType());
                        continue;
                    }
                    return action;
                }
                case INVALID:
                    inputPort.printLine(command.getError());
                    continue;
                default:
                    inputPort.printLine("Unsupported command.");
                    continue;
            }
        }
    }

    private Action promptForBuildAction(Board board, RuleChecker ruleChecker, GamePhase phase) {
        ActionType type = chooseBuildType(phase);
        if (type == null) {
            return null;
        }

        while (true) {
            Action candidate = readBuildTarget(type, board);
            if (candidate == null) {
                return null;
            }

            if (ruleChecker.isLegal(candidate, board, this, phase)) {
                return candidate;
            }

            inputPort.printLine("Invalid target for " + describeBuildType(type) + ". Check legal targets and try again.");
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
            if (input == null) {
                return null;
            }
            String token = input.trim().toLowerCase();
            if (token.isEmpty()) {
                return null;
            }
            switch (token) {
                case "s":
                case "settlement":
                    return ActionType.BUILD_SETTLEMENT;
                case "c":
                case "city":
                    return ActionType.BUILD_CITY;
                case "r":
                case "road":
                    return ActionType.BUILD_ROAD;
                default:
                    inputPort.printLine("Unknown build type. Use s, c, or r.");
                    break;
            }
        }
    }

    private Action readBuildTarget(ActionType type, Board board) {
        if (type == ActionType.BUILD_ROAD) {
            while (true) {
                String raw = inputPort.readLine("Road endpoints [from,to] or [from to] (Enter=cancel): ");
                if (raw == null || raw.trim().isEmpty()) {
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
            String raw = inputPort.readLine(describeBuildType(type) + " node id (Enter=cancel): ");
            if (raw == null || raw.trim().isEmpty()) {
                return null;
            }
            try {
                return new Action(type, Integer.parseInt(raw.trim()));
            } catch (NumberFormatException ex) {
                inputPort.printLine("Node id must be an integer.");
            }
        }
    }

    private Action actionFromBuildCommand(HumanCommand command, Board board) {
        switch (command.getType()) {
            case BUILD_SETTLEMENT:
                if (command.getNodeId() == null) {
                    inputPort.printLine("Settlement command missing node id.");
                    return null;
                }
                return new Action(ActionType.BUILD_SETTLEMENT, command.getNodeId());
            case BUILD_CITY:
                if (command.getNodeId() == null) {
                    inputPort.printLine("City command missing node id.");
                    return null;
                }
                return new Action(ActionType.BUILD_CITY, command.getNodeId());
            case BUILD_ROAD:
                if (command.getFromNodeId() == null || command.getToNodeId() == null) {
                    inputPort.printLine("Road command missing node ids.");
                    return null;
                }
                int edgeId = board.getEdgeIdBetweenNodes(command.getFromNodeId(), command.getToNodeId());
                if (edgeId < 0) {
                    inputPort.printLine(
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

    private void printPossibleActions(Board board, RuleChecker ruleChecker, GamePhase phase) {
        List<Action> legalActions = ruleChecker.getLegalActions(board, this, phase);
        List<Integer> settlementNodes = new ArrayList<>();
        List<Integer> cityNodes = new ArrayList<>();
        List<String> roadPairs = new ArrayList<>();

        for (Action action : legalActions) {
            if (!isBuildTypeAllowedInPhase(action.getActionType(), phase)) {
                continue;
            }
            if (action.getActionType() == ActionType.BUILD_SETTLEMENT) {
                settlementNodes.add(action.getTargetId());
            } else if (action.getActionType() == ActionType.BUILD_CITY) {
                cityNodes.add(action.getTargetId());
            } else if (action.getActionType() == ActionType.BUILD_ROAD) {
                Edge edge = board.getEdge(action.getTargetId());
                if (edge != null) {
                    roadPairs.add(edge.getNodeA() + "-" + edge.getNodeB());
                }
            }
        }

        inputPort.printLine("Possible actions now: Enter=pass, ls=list, a=show actions, b=build.");
        if (!settlementNodes.isEmpty()) {
            inputPort.printLine("Settlement nodes: " + summarizeList(settlementNodes));
        }
        if (!cityNodes.isEmpty()) {
            inputPort.printLine("City nodes: " + summarizeList(cityNodes));
        }
        if (!roadPairs.isEmpty()) {
            inputPort.printLine("Road endpoints: " + summarizeList(roadPairs));
        }
        if (settlementNodes.isEmpty() && cityNodes.isEmpty() && roadPairs.isEmpty()) {
            inputPort.printLine("No legal build actions available.");
        }
    }

    private void printPossibleTargetsForActionType(
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
                inputPort.printLine("No legal road targets available.");
            } else {
                inputPort.printLine("Road endpoints: " + summarizeList(roadPairs));
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
            inputPort.printLine("No legal " + describeBuildType(actionType).toLowerCase() + " targets available.");
            return;
        }

        inputPort.printLine(describeBuildType(actionType) + " nodes: " + summarizeList(nodeTargets));
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

    private static boolean isBuildTypeAllowedInPhase(ActionType type, GamePhase phase) {
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
                .append(getResourceHand().getCount(type));
        }
        sb.append(" (total=").append(getResourceHand().totalCards()).append(")");
        return sb.toString();
    }
}
