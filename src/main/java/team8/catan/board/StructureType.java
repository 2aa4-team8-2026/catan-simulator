package team8.catan.board;

import java.util.Map;

public enum StructureType {
    ROAD {
        @Override
        public Map<ResourceType, Integer> cost() {
            return Map.of(
                    ResourceType.BRICK, 1,
                    ResourceType.LUMBER, 1
            );
        }
    },
    SETTLEMENT {
        @Override
        public Map<ResourceType, Integer> cost() {
            return Map.of(
                    ResourceType.BRICK, 1,
                    ResourceType.LUMBER, 1,
                    ResourceType.WOOL, 1,
                    ResourceType.GRAIN, 1
            );
        }
    },
    CITY {
        @Override
        public Map<ResourceType, Integer> cost() {
            return Map.of(
                    ResourceType.ORE, 3,
                    ResourceType.GRAIN, 2
            );
        }
    };

    public abstract Map<ResourceType, Integer> cost();
}
