package team8.catan.players;

import org.junit.jupiter.api.Test;
import team8.catan.actions.ActionType;
import team8.catan.board.ResourceType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceHandTest {
    @Test
    void ResourceHand_totalCardsSumsAcrossResourceTypes() {
        ResourceHand hand = new ResourceHand();

        hand.add(ResourceType.BRICK, 2);
        hand.add(ResourceType.LUMBER, 1);
        hand.add(ResourceType.ORE, 3);

        assertEquals(6, hand.totalCards());
        assertEquals(0, hand.getCount(ResourceType.WOOL));
        assertEquals(5, hand.snapshot().size());
    }

    @Test
    void ResourceHand_canAffordSettlementAtExactCost() {
        ResourceHand hand = new ResourceHand();

        hand.add(ResourceType.BRICK, 1);
        hand.add(ResourceType.LUMBER, 1);
        hand.add(ResourceType.WOOL, 1);
        hand.add(ResourceType.GRAIN, 1);

        assertTrue(hand.canAfford(ActionType.BUILD_SETTLEMENT));
        assertFalse(hand.canAfford(ActionType.BUILD_CITY));
    }

    @Test
    void ResourceHand_cannotAffordSettlementWhenOneRequiredResourceIsMissing() {
        ResourceHand hand = new ResourceHand();

        hand.add(ResourceType.BRICK, 1);
        hand.add(ResourceType.LUMBER, 1);
        hand.add(ResourceType.WOOL, 1);

        assertFalse(hand.canAfford(ActionType.BUILD_SETTLEMENT));
        assertEquals(0, hand.discardRandomCards(0, new java.util.Random(0)));
        ResourceHand emptyHand = new ResourceHand();
        assertNull(emptyHand.removeRandomCard(new java.util.Random(0)));
    }

    @Test
    void ResourceHand_spendForCitySubtractsOnlyCityCost() {
        ResourceHand hand = new ResourceHand();

        hand.add(ResourceType.ORE, 3);
        hand.add(ResourceType.GRAIN, 2);
        hand.add(ResourceType.BRICK, 1);
        hand.add(ResourceType.LUMBER, 1);

        hand.spendFor(ActionType.BUILD_CITY);

        assertEquals(2, hand.totalCards());
        assertTrue(hand.canAfford(ActionType.BUILD_ROAD));
        assertFalse(hand.canAfford(ActionType.BUILD_CITY));
        hand.add(ResourceType.WOOL, 1);
        hand.add(ResourceType.BRICK, 1);
        assertNotNull(hand.removeRandomCard(new java.util.Random(0)));
        assertTrue(hand.discardRandomCards(2, new java.util.Random(1)) >= 1);
    }
}
