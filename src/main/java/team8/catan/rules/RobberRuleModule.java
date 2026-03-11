package team8.catan.rules;

import team8.catan.board.Board;
import team8.catan.gameplay.GamePhase;
import team8.catan.players.Player;

import java.util.List;
import java.util.Objects;

public class RobberRuleModule implements RuleModule {
    private final RobberService robberService;

    public RobberRuleModule() {
        this(new RobberService());
    }

    public RobberRuleModule(RobberService robberService) {
        this.robberService = Objects.requireNonNull(robberService, "robberService");
    }

    @Override
    public void onDiceRolled(
        int diceRoll,
        Player roller,
        Board board,
        List<? extends Player> players,
        GamePhase phase
    ) {
        if (phase != GamePhase.RUNNING || diceRoll != 7) {
            return;
        }
        robberService.resolveRobber(roller, board, players);
    }
}
