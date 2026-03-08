package team8.catan.support;

import team8.catan.actions.Action;
import team8.catan.board.Board;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;
import team8.catan.rules.RuleChecker;

public final class TestPlayer extends Player {
    public TestPlayer(int id) {
        super(id);
    }

    @Override
    public Action chooseAction(Board board, RuleChecker ruleChecker, GamePhase phase) {
        return null;
    }
}
