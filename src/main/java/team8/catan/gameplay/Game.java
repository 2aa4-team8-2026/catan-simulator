package team8.catan.gameplay;

import team8.catan.actions.*;
import team8.catan.board.Board;
import team8.catan.gameplay.commands.BuildCityCommand;
import team8.catan.gameplay.commands.BuildRoadCommand;
import team8.catan.gameplay.commands.BuildSettlementCommand;
import team8.catan.gameplay.commands.CommandHistory;
import team8.catan.gameplay.commands.TurnCommand;
import team8.catan.gameplay.commands.TurnResolutionCommand;
import team8.catan.gameplay.commands.UndoableCommand;
import team8.catan.players.Player;
import team8.catan.rules.RobberService;
import team8.catan.rules.RuleChecker;
import team8.catan.dice.Dice;
import team8.catan.dice.TwoDice;
import team8.catan.logging.ActionLogger;
import team8.catan.logging.ConsoleActionLogger;
import team8.catan.logging.GameStateWriter;
import team8.catan.logging.RoadPlacement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Game {
    private final Board board;
    private final List<Player> players;
    private final RuleChecker ruleChecker;
    private final Dice dice;
    private final int maxRounds;
    private final int victoryPointsToWin;
    private final CommandHistory commandHistory;
    private final ActionLogger actionLogger;
    private final StepForwardGate stepForwardGate;
    private final GameStateWriter stateWriter;
    private final List<RoadPlacement> roadPlacementOrder;
    private final RobberService robberService;

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
            (ignoredBoard, ignoredPlayers, ignoredRoadOrder) -> { },
            new RobberService()
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
            (ignoredBoard, ignoredPlayers, ignoredRoadOrder) -> { },
            new RobberService()
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
        this(
            board,
            players,
            ruleChecker,
            maxRounds,
            victoryPointsToWin,
            dice,
            actionLogger,
            stepForwardGate,
            stateWriter,
            new RobberService()
        );
    }

    Game(
        Board board,
        List<Player> players,
        RuleChecker ruleChecker,
        int maxRounds,
        int victoryPointsToWin,
        Dice dice,
        ActionLogger actionLogger,
        StepForwardGate stepForwardGate,
        GameStateWriter stateWriter,
        RobberService robberService
    ) {
        this.board = Objects.requireNonNull(board, "board");
        this.players = new ArrayList<>(Objects.requireNonNull(players, "players"));
        this.ruleChecker = Objects.requireNonNull(ruleChecker, "ruleChecker");
        this.dice = Objects.requireNonNull(dice, "dice");
        this.maxRounds = maxRounds;
        this.victoryPointsToWin = victoryPointsToWin;
        this.commandHistory = new CommandHistory();
        this.actionLogger = Objects.requireNonNull(actionLogger, "actionLogger");
        this.stepForwardGate = Objects.requireNonNull(stepForwardGate, "stepForwardGate");
        this.stateWriter = Objects.requireNonNull(stateWriter, "stateWriter");
        this.robberService = Objects.requireNonNull(robberService, "robberService");
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
        Action chosenAction = chooseActionHandlingControls(player);
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
        TurnCommand turnCommand = new TurnCommand(
            round,
            player.getId(),
            diceRoll,
            buildTurnResolutionCommand(player, diceRoll)
        );
        boolean turnStarted = commandHistory.execute(turnCommand);
        Action action = chooseActionHandlingControls(player, turnCommand);

        // double check if action is valid
        if (action == null || !ruleChecker.isLegal(action, board, player, phase)) {
            action = new Action(ActionType.PASS, ActionTarget.NO_TARGET_ID);
        }

        boolean applied = turnStarted && executeTurnAction(turnCommand, player, action);
        actionLogger.logAction(round, player, action, applied);
        stateWriter.write(board, players, roadPlacementOrder);
    }

    private int rollDice() {
        return dice.roll();
    }

    private boolean executeAction(Player player, Action action, boolean chargeCost) {
        if (action.getActionType() == ActionType.PASS) {
            return true;
        }
        UndoableCommand command = buildCommand(player, action, chargeCost);
        return commandHistory.execute(command);
    }

    private UndoableCommand buildCommand(Player player, Action action, boolean chargeCost) {
        return switch (action.getActionType()) {
            case BUILD_ROAD -> new BuildRoadCommand(board, player, action, chargeCost, roadPlacementOrder);
            case BUILD_SETTLEMENT -> new BuildSettlementCommand(board, player, action, chargeCost, roadPlacementOrder);
            case BUILD_CITY -> new BuildCityCommand(board, player, action, chargeCost, roadPlacementOrder);
            default -> throw new IllegalStateException("No command registered for action type: " + action.getActionType());
        };
    }

    private Action chooseActionHandlingControls(Player player) {
        return chooseActionHandlingControls(player, null);
    }

    private Action chooseActionHandlingControls(Player player, TurnCommand activeTurn) {
        while (true) {
            Action action = player.chooseAction(board, ruleChecker, phase);
            if (action == null) {
                if (activeTurn != null && !activeTurn.isApplied()) {
                    actionLogger.logInfo("Redo the current turn before ending it.");
                    continue;
                }
                return null;
            }
            if (action.getActionType() == ActionType.UNDO) {
                handleUndo();
                continue;
            }
            if (action.getActionType() == ActionType.REDO) {
                handleRedo();
                continue;
            }
            if (activeTurn != null && !activeTurn.isApplied()) {
                actionLogger.logInfo("Redo the current turn before choosing an action.");
                continue;
            }
            return action;
        }
    }

    private boolean executeTurnAction(TurnCommand turnCommand, Player player, Action action) {
        if (action.getActionType() == ActionType.PASS) {
            return turnCommand.attachPlayerAction(action, null);
        }
        UndoableCommand actionCommand = buildCommand(player, action, true);
        return turnCommand.attachPlayerAction(action, actionCommand);
    }

    private TurnResolutionCommand buildTurnResolutionCommand(Player player, int diceRoll) {
        return new TurnResolutionCommand(board, player, players, diceRoll, robberService);
    }

    private void handleUndo() {
        if (commandHistory.undo()) {
            actionLogger.logInfo("Undo applied.");
            stateWriter.write(board, players, roadPlacementOrder);
            return;
        }
        actionLogger.logInfo("Nothing to undo.");
    }

    private void handleRedo() {
        if (commandHistory.redo()) {
            actionLogger.logInfo("Redo applied.");
            stateWriter.write(board, players, roadPlacementOrder);
            return;
        }
        actionLogger.logInfo("Nothing to redo.");
    }
}
