package team8.catan.players.strategy;

import team8.catan.actions.Action;
import team8.catan.board.Board;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;
import team8.catan.rules.RuleChecker;

public interface ActionSelectionPolicy {
    Action chooseAction(Board board, Player player, RuleChecker ruleChecker, GamePhase phase);
}
