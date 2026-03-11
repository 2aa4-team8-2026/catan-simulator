package team8.catan.actions;

import org.junit.jupiter.api.Test;
import team8.catan.board.Board;
import team8.catan.board.Node;
import team8.catan.board.ResourceType;
import team8.catan.board.StructureType;
import team8.catan.support.TestPlayer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionExecutorsTest {
    @Test
    void BuildSettlementExecutor_placesSettlementAwardsVpAndChargesCost() {
        Board board = new Board(List.of(new Node(0)), List.of());
        TestPlayer player = new TestPlayer(3);
        player.getResourceHand().add(ResourceType.BRICK, 1);
        player.getResourceHand().add(ResourceType.LUMBER, 1);
        player.getResourceHand().add(ResourceType.WOOL, 1);
        player.getResourceHand().add(ResourceType.GRAIN, 1);
        player.getResourceHand().add(ResourceType.ORE, 1);

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
        assertEquals(1, player.getResourceHand().totalCards());
    }

    @Test
    void BuildCityExecutor_upgradesOwnedSettlementAwardsVpAndChargesCost() {
        Board board = new Board(List.of(new Node(0)), List.of());
        board.getNode(0).placeSettlement(4);
        TestPlayer player = new TestPlayer(4);
        player.addVictoryPoints(1);
        player.getResourceHand().add(ResourceType.ORE, 3);
        player.getResourceHand().add(ResourceType.GRAIN, 2);
        player.getResourceHand().add(ResourceType.BRICK, 1);
        player.getResourceHand().add(ResourceType.LUMBER, 1);

        boolean applied = new BuildCityExecutor().execute(
            board,
            player,
            new Action(ActionType.BUILD_CITY, 0),
            true
        );

        assertTrue(applied);
        assertEquals(StructureType.CITY, board.getNode(0).getStructureType());
        assertEquals(2, player.getVictoryPoints());
        assertEquals(2, player.getResourceHand().totalCards());
        assertTrue(player.getResourceHand().canAfford(ActionType.BUILD_ROAD));
    }
}
