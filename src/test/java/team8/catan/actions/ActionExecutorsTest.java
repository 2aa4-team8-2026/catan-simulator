package team8.catan.actions;

import org.junit.jupiter.api.Test;
import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.board.StructureType;
import team8.catan.support.TestPlayer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionExecutorsTest {
    @Test
    void BuildSettlementExecutor_placesSettlementAwardsVpAndChargesCost() {
        Board board = new Board(List.of(new Node(0)), List.of());
        TestPlayer player = new TestPlayer(3);
        player.grantResource(ResourceType.BRICK, 1);
        player.grantResource(ResourceType.LUMBER, 1);
        player.grantResource(ResourceType.WOOL, 1);
        player.grantResource(ResourceType.GRAIN, 1);
        player.grantResource(ResourceType.ORE, 1);

        boolean applied = new BuildSettlementExecutor().execute(
            board,
            player,
            new Action(ActionType.BUILD_SETTLEMENT, 0),
            true
        );

        assertTrue(applied);
        assertEquals(3, board.getNode(0).getOwnerId());
        assertEquals(StructureType.SETTLEMENT, board.getNode(0).getStructureType());
        assertEquals(1, player.getVictoryPoints());
        assertEquals(1, player.getTotalResourceCards());
        assertFalse(new BuildSettlementExecutor().execute(
            board,
            player,
            new Action(ActionType.BUILD_SETTLEMENT, 0),
            true
        ));
        assertEquals(ActionType.BUILD_SETTLEMENT, new BuildSettlementExecutor().supportedType());
    }

    @Test
    void BuildCityExecutor_upgradesOwnedSettlementAwardsVpAndChargesCost() {
        Board board = new Board(List.of(new Node(0), new Node(1)), List.of(new team8.catan.board.Edge(0, 0, 1)));
        board.getNode(0).placeSettlement(4);
        TestPlayer player = new TestPlayer(4);
        player.addVictoryPoints(1);
        player.grantResource(ResourceType.ORE, 3);
        player.grantResource(ResourceType.GRAIN, 2);
        player.grantResource(ResourceType.BRICK, 1);
        player.grantResource(ResourceType.LUMBER, 1);

        boolean applied = new BuildCityExecutor().execute(
            board,
            player,
            new Action(ActionType.BUILD_CITY, 0),
            true
        );

        assertTrue(applied);
        assertEquals(StructureType.CITY, board.getNode(0).getStructureType());
        assertEquals(2, player.getVictoryPoints());
        assertEquals(2, player.getTotalResourceCards());
        assertTrue(player.canAfford(ActionType.BUILD_ROAD));
        assertFalse(new BuildCityExecutor().execute(
            board,
            new TestPlayer(5),
            new Action(ActionType.BUILD_CITY, 0),
            true
        ));
        assertTrue(new BuildRoadExecutor().execute(
            board,
            player,
            new Action(ActionType.BUILD_ROAD, 0),
            false
        ));
        assertEquals(4, board.getEdge(0).getRoadOwnerId());
        assertTrue(new PassExecutor().execute(board, player, new Action(ActionType.PASS, -1), true));
        assertEquals(ActionType.BUILD_CITY, new BuildCityExecutor().supportedType());
        assertEquals(ActionType.BUILD_ROAD, new BuildRoadExecutor().supportedType());
        assertEquals(ActionType.PASS, new PassExecutor().supportedType());
    }
}
