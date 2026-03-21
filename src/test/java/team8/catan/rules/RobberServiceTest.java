package team8.catan.rules;

import org.junit.jupiter.api.Test;
import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.board.StructureType;
import team8.catan.board.Tile;
import team8.catan.support.TestPlayer;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RobberServiceTest {
    @Test
    void resolveOutcome_andApplyOutcome_handleDiscardAndStealDeterministically() {
        Board board = new Board(
            List.of(new Node(0), new Node(1)),
            List.of(new Edge(0, 0, 1)),
            List.of(new Tile(0, 0, 0, 0, ResourceType.BRICK, 6, new int[] { 0, 1 })),
            -1
        );
        board.getNode(1).restoreState(1, StructureType.SETTLEMENT);

        TestPlayer roller = new TestPlayer(0);
        TestPlayer victim = new TestPlayer(1);
        victim.grantResource(ResourceType.WOOL, 4);
        victim.grantResource(ResourceType.BRICK, 4);

        RobberService service = new RobberService(new Random(0));
        RobberOutcome outcome = service.resolveOutcome(roller, board, List.of(roller, victim));

        assertEquals(0, outcome.robberTileId());
        assertEquals(1, outcome.victimPlayerId());

        service.applyOutcome(outcome, board, List.of(roller, victim));

        assertEquals(0, board.getRobberTileId());
        assertEquals(1, roller.getTotalResourceCards());
        assertEquals(3, victim.getTotalResourceCards());
    }

    @Test
    void resolveOutcome_handlesBoardWithoutTiles() {
        Board board = new Board(List.of(new Node(0)), List.of());
        TestPlayer roller = new TestPlayer(0);
        RobberService service = new RobberService(new Random(0));

        RobberOutcome outcome = service.resolveOutcome(roller, board, List.of(roller));

        assertEquals(-1, outcome.robberTileId());
        assertEquals(null, outcome.victimPlayerId());
        assertEquals(null, outcome.stolenResource());
    }
}
