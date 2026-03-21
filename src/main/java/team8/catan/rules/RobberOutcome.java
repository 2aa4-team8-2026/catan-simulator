package team8.catan.rules;

import team8.catan.board.ResourceType;

import java.util.Map;

public record RobberOutcome(
    int robberTileId,
    Integer victimPlayerId,
    ResourceType stolenResource,
    Map<Integer, Map<ResourceType, Integer>> resultingResources
) {
}
