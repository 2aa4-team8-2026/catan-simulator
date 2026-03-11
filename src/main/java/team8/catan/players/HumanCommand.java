package team8.catan.players;

import team8.catan.actions.Action;
import team8.catan.actions.ActionTarget;
import team8.catan.actions.ActionType;
import team8.catan.board.Board;
import team8.catan.gameplay.GamePhase;
import team8.catan.rules.RuleChecker;

public class HumanCommand {
    public enum HumanCommandType {
        ROLL {
            @Override
            Action executeAction(
                HumanCommand command,
                HumanPlayer player,
                Board board,
                RuleChecker ruleChecker,
                GamePhase phase
            ) {
                player.printLine("Dice auto-rolls at turn start. Use b, ls, or Enter.");
                return null;
            }
        },
        GO {
            @Override
            Action executeAction(
                HumanCommand command,
                HumanPlayer player,
                Board board,
                RuleChecker ruleChecker,
                GamePhase phase
            ) {
                if (HumanPlayer.isMandatorySetupPhase(phase)) {
                    player.printLine("Setup placement is mandatory. Choose a legal build target.");
                    return null;
                }
                return new Action(ActionType.PASS, ActionTarget.NO_TARGET_ID);
            }
        },
        LIST {
            @Override
            Action executeAction(
                HumanCommand command,
                HumanPlayer player,
                Board board,
                RuleChecker ruleChecker,
                GamePhase phase
            ) {
                player.printResourceSummary();
                return null;
            }
        },
        BUILD_MENU {
            @Override
            Action executeAction(
                HumanCommand command,
                HumanPlayer player,
                Board board,
                RuleChecker ruleChecker,
                GamePhase phase
            ) {
                return player.promptForBuildAction(board, ruleChecker, phase);
            }
        },
        BUILD_SETTLEMENT {
            @Override
            Action executeAction(
                HumanCommand command,
                HumanPlayer player,
                Board board,
                RuleChecker ruleChecker,
                GamePhase phase
            ) {
                return executeDirectBuild(command, player, board, ruleChecker, phase);
            }
        },
        BUILD_CITY {
            @Override
            Action executeAction(
                HumanCommand command,
                HumanPlayer player,
                Board board,
                RuleChecker ruleChecker,
                GamePhase phase
            ) {
                return executeDirectBuild(command, player, board, ruleChecker, phase);
            }
        },
        BUILD_ROAD {
            @Override
            Action executeAction(
                HumanCommand command,
                HumanPlayer player,
                Board board,
                RuleChecker ruleChecker,
                GamePhase phase
            ) {
                return executeDirectBuild(command, player, board, ruleChecker, phase);
            }
        },
        INVALID {
            @Override
            Action executeAction(
                HumanCommand command,
                HumanPlayer player,
                Board board,
                RuleChecker ruleChecker,
                GamePhase phase
            ) {
                player.printLine(command.getError());
                return null;
            }
        };

        abstract Action executeAction(
            HumanCommand command,
            HumanPlayer player,
            Board board,
            RuleChecker ruleChecker,
            GamePhase phase
        );

        private static Action executeDirectBuild(
            HumanCommand command,
            HumanPlayer player,
            Board board,
            RuleChecker ruleChecker,
            GamePhase phase
        ) {
            Action action = player.actionFromBuildCommand(command, board);
            if (action == null) {
                return null;
            }
            if (!HumanPlayer.isBuildTypeAllowedInPhase(action.getActionType(), phase)) {
                player.printLine("That build type is not available in this phase.");
                return null;
            }
            if (!ruleChecker.isLegal(action, board, player, phase)) {
                player.printLine("That build is not legal right now.");
                player.printPossibleTargetsForActionType(board, ruleChecker, phase, action.getActionType());
                return null;
            }
            return action;
        }
    }

    private final HumanCommandType type;
    private final Integer nodeId;
    private final Integer fromNodeId;
    private final Integer toNodeId;
    private final String error;

    private HumanCommand(HumanCommandType type, Integer nodeId, Integer fromNodeId, Integer toNodeId, String error) {
        this.type = type;
        this.nodeId = nodeId;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.error = error;
    }

    public static HumanCommand roll() {
        return new HumanCommand(HumanCommandType.ROLL, null, null, null, null);
    }

    public static HumanCommand go() {
        return new HumanCommand(HumanCommandType.GO, null, null, null, null);
    }

    public static HumanCommand list() {
        return new HumanCommand(HumanCommandType.LIST, null, null, null, null);
    }

    public static HumanCommand buildMenu() {
        return new HumanCommand(HumanCommandType.BUILD_MENU, null, null, null, null);
    }

    public static HumanCommand buildSettlement(int nodeId) {
        return new HumanCommand(HumanCommandType.BUILD_SETTLEMENT, nodeId, null, null, null);
    }

    public static HumanCommand buildCity(int nodeId) {
        return new HumanCommand(HumanCommandType.BUILD_CITY, nodeId, null, null, null);
    }

    public static HumanCommand buildRoad(int fromNodeId, int toNodeId) {
        return new HumanCommand(HumanCommandType.BUILD_ROAD, null, fromNodeId, toNodeId, null);
    }

    public static HumanCommand invalid(String error) {
        return new HumanCommand(HumanCommandType.INVALID, null, null, null, error);
    }

    public HumanCommandType getType() {
        return type;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public Integer getFromNodeId() {
        return fromNodeId;
    }

    public Integer getToNodeId() {
        return toNodeId;
    }

    public String getError() {
        return error;
    }

    public Action executeAction(HumanPlayer player, Board board, RuleChecker ruleChecker, GamePhase phase) {
        return type.executeAction(this, player, board, ruleChecker, phase);
    }
}
