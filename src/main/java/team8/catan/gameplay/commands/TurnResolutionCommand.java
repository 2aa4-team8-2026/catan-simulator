package team8.catan.gameplay.commands;

import team8.catan.board.Board;
import team8.catan.players.Player;
import team8.catan.rules.RobberService;

import java.util.List;

public class TurnResolutionCommand extends CompositeCommand {
    private final int diceRoll;

    public TurnResolutionCommand(
        Board board,
        Player roller,
        List<? extends Player> players,
        int diceRoll,
        RobberService robberService
    ) {
        super(List.of(buildChildCommand(board, roller, players, diceRoll, robberService)));
        this.diceRoll = diceRoll;
    }

    public int getDiceRoll() {
        return diceRoll;
    }

    private static UndoableCommand buildChildCommand(
        Board board,
        Player roller,
        List<? extends Player> players,
        int diceRoll,
        RobberService robberService
    ) {
        if (diceRoll == 7) {
            return new RobberResolutionCommand(board, roller, players, robberService);
        }
        return new ResourceDistributionCommand(board, players, diceRoll);
    }
}
