package team8.catan.players;

import org.junit.jupiter.api.Test;
import team8.catan.actions.ActionType;
import team8.catan.board.ResourceType;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    }

    @Test
    void ResourceHand_canAffordSettlementAtExactCost() {
        ResourceHand hand = new ResourceHand();

        hand.add(ResourceType.BRICK, 1);
        hand.add(ResourceType.LUMBER, 1);
        hand.add(ResourceType.WOOL, 1);
        hand.add(ResourceType.GRAIN, 1);

        assertTrue(hand.canAfford(ActionType.BUILD_SETTLEMENT));
    }

    @Test
    void ResourceHand_cannotAffordSettlementWhenOneRequiredResourceIsMissing() {
        ResourceHand hand = new ResourceHand();

        hand.add(ResourceType.BRICK, 1);
        hand.add(ResourceType.LUMBER, 1);
        hand.add(ResourceType.WOOL, 1);

        assertFalse(hand.canAfford(ActionType.BUILD_SETTLEMENT));
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
    }

    @Test
    void ResourceHand_restoreAndSnapshotPreserveCountsAndDefaultMissingTypesToZero() {
        ResourceHand hand = new ResourceHand();
        hand.add(ResourceType.BRICK, 2);
        hand.add(ResourceType.WOOL, 1);

        Map<ResourceType, Integer> snapshot = hand.snapshot();
        hand.restore(Map.of(ResourceType.ORE, 3));

        assertEquals(0, hand.getCount(ResourceType.BRICK));
        assertEquals(0, hand.getCount(ResourceType.WOOL));
        assertEquals(3, hand.getCount(ResourceType.ORE));

        hand.restore(snapshot);
        assertEquals(2, hand.getCount(ResourceType.BRICK));
        assertEquals(1, hand.getCount(ResourceType.WOOL));
    }

    @Test
    void ResourceHand_discardRandomCardsAndRemoveRandomCardHandleEmptyAndOverDiscard() {
        ResourceHand hand = new ResourceHand();

        assertEquals(0, hand.discardRandomCards(0, new Random(0)));
        assertEquals(0, hand.discardRandomCards(2, new Random(0)));
        assertNull(hand.removeRandomCard(new Random(0)));

        hand.add(ResourceType.BRICK, 1);
        assertEquals(1, hand.discardRandomCards(3, new Random(0)));
        assertEquals(0, hand.totalCards());
        assertNull(hand.removeRandomCard(new Random(0)));
    }
}
