package rules;

import board.Board;
import actions.Action;
import actions.ActionFeature;
import actions.ActionType;
import players.Player;
import gameplay.GamePhase;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RuleChecker {
    private final List<RuleModule> modules;

    public RuleChecker() {
        this(List.of(
            new SettlementDistanceRuleModule(),
            new SettlementRoadConnectionRuleModule(),
            new CityRequiresSettlementRuleModule(),
            new RoadConnectionRuleModule()
        ));
    }

    public RuleChecker(List<RuleModule> modules) {
        this.modules = new ArrayList<>(Objects.requireNonNull(modules, "modules"));
    }

    public List<Action> getLegalActions(Board board, Player player, GamePhase phase) {
        List<Action> candidates = new ArrayList<>();

        for (ActionType actionType : ActionType.values()) {
            if (actionType.getFeature() != ActionFeature.CORE) {
                continue;
            }

            if (actionType != ActionType.PASS && !isAffordableForPhase(actionType, player, phase)) {
                continue;
            }

            int[] targets = actionType.getValidTargets(board, player.getId());
            for (int targetId : targets) {
                candidates.add(new Action(actionType, targetId));
            }
        }

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

    public void onDiceRolled(int diceRoll, Board board, List<? extends Player> players, GamePhase phase) {
        for (RuleModule module : modules) {
            module.onDiceRolled(diceRoll, board, players, phase);
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
        if (!isTargetValid(action, board, player.getId())) {
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
        return player.getResourceHand().canAfford(actionType);
    }

    private boolean isTargetValid(Action action, Board board, int playerId) {
        int[] validTargets = action.getActionType().getValidTargets(board, playerId);
        int targetId = action.getTargetId();
        for (int validTarget : validTargets) {
            if (validTarget == targetId) {
                return true;
            }
        }
        return false;
    }
}
