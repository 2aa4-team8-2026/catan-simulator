package team8.catan.rules;

import team8.catan.board.Board;
import team8.catan.actions.Action;
import team8.catan.actions.ActionFeature;
import team8.catan.actions.ActionType;
import team8.catan.players.Player;
import team8.catan.gameplay.GamePhase;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RuleChecker {
    private final List<RuleModule> modules;
    private final ActionGenerationService actionGenerationService;

    public RuleChecker() {
        this(new ActionGenerationService(), List.of(
            new SettlementDistanceRuleModule(),
            new SettlementRoadConnectionRuleModule(),
            new CityRequiresSettlementRuleModule(),
            new RoadConnectionRuleModule(),
            new RobberRuleModule()
        ));
    }

    public RuleChecker(List<RuleModule> modules) {
        this(new ActionGenerationService(), modules);
    }

    public RuleChecker(ActionGenerationService actionGenerationService, List<RuleModule> modules) {
        this.actionGenerationService = Objects.requireNonNull(actionGenerationService, "actionGenerationService");
        this.modules = new ArrayList<>(Objects.requireNonNull(modules, "modules"));
    }

    public List<Action> getLegalActions(Board board, Player player, GamePhase phase) {
        List<Action> candidates = new ArrayList<>(actionGenerationService.generate(board, player, phase));
        Set<Action> deduplicated = new LinkedHashSet<>(candidates);
        List<Action> legal = new ArrayList<>();
        for (Action action : deduplicated) {
            if (isLegalInternal(action, board, player, phase)) {
                legal.add(action);
            }
        }

        return legal;
    }

    public boolean isLegal(Action action, Board board, Player player, GamePhase phase) {
        return isLegalInternal(action, board, player, phase);
    }

    public void onDiceRolled(
        int diceRoll,
        Player roller,
        Board board,
        List<? extends Player> players,
        GamePhase phase
    ) {
        for (RuleModule module : modules) {
            module.onDiceRolled(diceRoll, roller, board, players, phase);
        }
    }

    private boolean isLegalInternal(Action action, Board board, Player player, GamePhase phase) {
        if (action == null) {
            return false;
        }
        if (action.getActionType().getFeature() != ActionFeature.CORE) {
            return false;
        }
        if (action.getActionType() != ActionType.PASS && !isAffordableForPhase(action.getActionType(), player, phase)) {
            return false;
        }
        if (!isTargetValid(action, board, player, phase)) {
            return false;
        }

        for (RuleModule module : modules) {
            if (!module.isLegal(action, board, player, phase)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAffordableForPhase(ActionType actionType, Player player, GamePhase phase) {
        if (phase == GamePhase.SETUP_SETTLEMENT && actionType == ActionType.BUILD_SETTLEMENT) {
            return true;
        }
        if (phase == GamePhase.SETUP_ROAD && actionType == ActionType.BUILD_ROAD) {
            return true;
        }
        return player.canAfford(actionType);
    }

    private boolean isTargetValid(Action action, Board board, Player player, GamePhase phase) {
        return actionGenerationService.isCandidateTarget(action, board, player, phase);
    }
}
