package team8.catan.gameplay;

import team8.catan.actions.*;
import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.board.StructureType;
import team8.catan.players.Player;
import team8.catan.rules.RuleChecker;
import team8.catan.dice.Dice;
import team8.catan.dice.TwoDice;
import team8.catan.logging.ActionLogger;
import team8.catan.logging.ConsoleActionLogger;
import team8.catan.logging.GameStateWriter;
import team8.catan.logging.RoadPlacement;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Game {
    private final Board board;
    private final List<Player> players;
    private final RuleChecker ruleChecker;
    private final Dice dice;
    private final int maxRounds;
    private final int victoryPointsToWin;
    private final Map<ActionType, ActionExecutor> executors;
    private final ActionLogger actionLogger;
    private final StepForwardGate stepForwardGate;
    private final GameStateWriter stateWriter;
    private final List<RoadPlacement> roadPlacementOrder;

    private int round;
    private GamePhase phase;

    public Game(Board board, List<Player> players, RuleChecker ruleChecker, int maxRounds, int victoryPointsToWin) {
        this(
            board,
            players,
            ruleChecker,
            maxRounds,
            victoryPointsToWin,
            new TwoDice(),
            new ConsoleActionLogger(),
            new NoOpStepForwardGate(),
            (ignoredBoard, ignoredPlayers, ignoredRoadOrder) -> { }
        );
    }

    public Game(
        Board board,
        List<Player> players,
        RuleChecker ruleChecker,
        int maxRounds,
        int victoryPointsToWin,
        Dice dice
    ) {
        this(
            board,
            players,
            ruleChecker,
            maxRounds,
            victoryPointsToWin,
            dice,
            new ConsoleActionLogger(),
            new NoOpStepForwardGate(),
            (ignoredBoard, ignoredPlayers, ignoredRoadOrder) -> { }
        );
    }

    public Game(
        Board board,
        List<Player> players,
        RuleChecker ruleChecker,
        int maxRounds,
        int victoryPointsToWin,
        Dice dice,
        ActionLogger actionLogger,
        StepForwardGate stepForwardGate,
        GameStateWriter stateWriter
    ) {
        this.board = Objects.requireNonNull(board, "board");
        this.players = new ArrayList<>(Objects.requireNonNull(players, "players"));
        this.ruleChecker = Objects.requireNonNull(ruleChecker, "ruleChecker");
        this.dice = Objects.requireNonNull(dice, "dice");
        this.maxRounds = maxRounds;
        this.victoryPointsToWin = victoryPointsToWin;
        this.executors = buildExecutors();
        this.actionLogger = Objects.requireNonNull(actionLogger, "actionLogger");
        this.stepForwardGate = Objects.requireNonNull(stepForwardGate, "stepForwardGate");
        this.stateWriter = Objects.requireNonNull(stateWriter, "stateWriter");
        this.roadPlacementOrder = new ArrayList<>();
        this.round = 0;
        this.phase = GamePhase.SETUP_SETTLEMENT;
    }

    public void run() {
        runSetupPhase();
        phase = GamePhase.RUNNING;

        while (this.round < maxRounds && !shouldTerminate()) {
            this.round++;
            for (Player player : players) {
                if (shouldTerminate()) {
                    break;
                }
                stepForwardGate.awaitGo(round, player, phase);
                executeTurn(player);
            }
            actionLogger.logRoundVictoryPoints(round, players);
        }
    }

    public boolean shouldTerminate() {
        for (Player player : players) {
            if (player.getVictoryPoints() >= victoryPointsToWin) {
                return true;
            }
        }
        return false;
    }

    public int getRound() {
        return round;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    private void runSetupPhase() {
        for (Player player : players) {
            phase = GamePhase.SETUP_SETTLEMENT;
            stepForwardGate.awaitGo(0, player, phase);
            executeSetupAction(player, ActionType.BUILD_SETTLEMENT);

            phase = GamePhase.SETUP_ROAD;
            stepForwardGate.awaitGo(0, player, phase);
            executeSetupAction(player, ActionType.BUILD_ROAD);
        }
    }

    private void executeSetupAction(Player player, ActionType requiredType) {
        Action chosenAction = player.chooseAction(board, ruleChecker, phase);
        if (chosenAction == null
            || chosenAction.getActionType() != requiredType
            || !ruleChecker.isLegal(chosenAction, board, player, phase)) {
            chosenAction = firstLegalAction(player, requiredType);
        }

        if (chosenAction != null && chosenAction.getActionType() == requiredType) {
            boolean applied = executeAction(player, chosenAction, false);
            actionLogger.logAction(0, player, chosenAction, applied);
            stateWriter.write(board, players, roadPlacementOrder);
            if (!applied) {
                Action fallback = firstLegalAction(player, requiredType);
                if (fallback != null && !fallback.equals(chosenAction)) {
                    boolean fallbackApplied = executeAction(player, fallback, false);
                    actionLogger.logAction(0, player, fallback, fallbackApplied);
                    stateWriter.write(board, players, roadPlacementOrder);
                }
            }
        }
    }

    private Action firstLegalAction(Player player, ActionType requiredType) {
        List<Action> legalActions = ruleChecker.getLegalActions(board, player, phase);
        for (Action action : legalActions) {
            if (action.getActionType() == requiredType) {
                return action;
            }
        }
        return null;
    }

    private void executeTurn(Player player) {
        int diceRoll = rollDice();

        ruleChecker.onDiceRolled(diceRoll, player, board, players, phase);
        if (diceRoll != 7) {
            distributeResources(diceRoll);
        }

        Action action = player.chooseAction(board, ruleChecker, phase);

        // double check if action is valid
        if (action == null || !ruleChecker.isLegal(action, board, player, phase)) {
            action = new Action(ActionType.PASS, ActionTarget.NO_TARGET_ID);
        }

        boolean applied = executeAction(player, action, true);
        actionLogger.logAction(round, player, action, applied);
        stateWriter.write(board, players, roadPlacementOrder);
    }

    private int rollDice() {
        return dice.roll();
    }

    private void distributeResources(int diceRoll) {
        // Simplified production model for this assignment scaffold:
        // each settlement gives 1 resource and each city gives 2 of a roll-derived resource.
        ResourceType producedType = resourceTypeForRoll(diceRoll);
        for (Node node : board.getNodes()) {
            if (node.getOwnerId() == Node.UNOWNED) {
                continue;
            }
            if (node.getStructureType() == null) {
                continue;
            }

            Player owner = getPlayerById(node.getOwnerId());
            if (owner == null) {
                continue;
            }

            int amount = node.getStructureType() == StructureType.CITY ? 2 : 1;
            owner.grantResource(producedType, amount);
        }
    }

    private boolean executeAction(Player player, Action action, boolean chargeCost) {
        ActionExecutor executor = executors.get(action.getActionType());
        if (executor == null) {
            throw new IllegalStateException("No executor registered for action type: " + action.getActionType());
        }
        boolean applied = executor.execute(board, player, action, chargeCost);
        if (applied && action.getActionType() == ActionType.BUILD_ROAD) {
            roadPlacementOrder.add(new RoadPlacement(action.getTargetId(), player.getId()));
        }
        return applied;
    }

    private Map<ActionType, ActionExecutor> buildExecutors() {
        List<ActionExecutor> executorList = List.of(
            new BuildRoadExecutor(),
            new BuildSettlementExecutor(),
            new BuildCityExecutor(),
            new PassExecutor()
        );

        Map<ActionType, ActionExecutor> registry = new EnumMap<>(ActionType.class);
        for (ActionExecutor executor : executorList) {
            ActionType actionType = executor.supportedType();
            ActionExecutor previous = registry.put(actionType, executor);
            if (previous != null) {
                throw new IllegalStateException("Duplicate executor registration for action type: " + actionType);
            }
        }

        for (ActionType actionType : ActionType.values()) {
            if (!registry.containsKey(actionType)) {
                throw new IllegalStateException("Missing executor for action type: " + actionType);
            }
        }

        return registry;
    }

    private ResourceType resourceTypeForRoll(int diceRoll) {
        ResourceType[] all = ResourceType.values();
        int index = Math.floorMod(diceRoll - 2, all.length);
        return all[index];
    }

    private Player getPlayerById(int playerId) {
        for (Player player : players) {
            if (player.getId() == playerId) {
                return player;
            }
        }
        return null;
    }
}
