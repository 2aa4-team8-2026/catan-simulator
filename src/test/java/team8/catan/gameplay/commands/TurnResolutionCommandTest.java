package team8.catan.gameplay.commands;

import org.junit.jupiter.api.Test;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.Node;
import team8.catan.players.Player;
import team8.catan.rules.RobberService;
import team8.catan.support.TestPlayer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TurnResolutionCommandTest {
    @Test
    void TurnResolutionCommand_usesResourceDistributionChildForNonSevenRolls() {
        InspectableTurnResolutionCommand command = new InspectableTurnResolutionCommand(board(), player(), 6);

        assertEquals(ResourceDistributionCommand.class, command.children().get(0).getClass());
        assertEquals(6, command.getDiceRoll());
    }

    @Test
    void TurnResolutionCommand_usesRobberResolutionChildForSevenRolls() {
        InspectableTurnResolutionCommand command = new InspectableTurnResolutionCommand(board(), player(), 7);

        assertEquals(RobberResolutionCommand.class, command.children().get(0).getClass());
        assertEquals(7, command.getDiceRoll());
    }

    private static Board board() {
        return new Board(List.of(new Node(0), new Node(1)), List.of(new Edge(0, 0, 1)));
    }

    private static Player player() {
        return new TestPlayer(0);
    }

    private static final class InspectableTurnResolutionCommand extends TurnResolutionCommand {
        private InspectableTurnResolutionCommand(Board board, Player roller, int diceRoll) {
            super(board, roller, List.of(roller), diceRoll, new RobberService());
        }

        private List<UndoableCommand> children() {
            return getChildren();
        }
    }
}
