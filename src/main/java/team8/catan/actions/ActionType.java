package actions;

import board.Board;
import board.ResourceType;
import board.StructureType;

import java.util.Map;

public enum ActionType {
    BUILD_ROAD(TargetKind.EDGE, ActionFeature.CORE) {
        @Override
        public Map<ResourceType, Integer> getCost() {
            return StructureType.ROAD.cost();
        }

        @Override
        public int[] getValidTargets(Board board, int playerId) {
            return board.getValidRoadTargets(playerId);
        }
    },
    BUILD_SETTLEMENT(TargetKind.NODE, ActionFeature.CORE) {
        @Override
        public Map<ResourceType, Integer> getCost() {
            return StructureType.SETTLEMENT.cost();
        }

        @Override
        public int[] getValidTargets(Board board, int playerId) {
            return board.getValidSettlementTargets(playerId);
        }
    },
    BUILD_CITY(TargetKind.NODE, ActionFeature.CORE) {
        @Override
        public Map<ResourceType, Integer> getCost() {
            return StructureType.CITY.cost();
        }

        @Override
        public int[] getValidTargets(Board board, int playerId) {
            return board.getValidCityTargets(playerId);
        }
    },
    PASS(TargetKind.NONE, ActionFeature.CORE) {
        @Override
        public Map<ResourceType, Integer> getCost() {
            return Map.of();
        }

        @Override
        public int[] getValidTargets(Board board, int playerId) {
            return new int[] { ActionTarget.NO_TARGET_ID };
        }
    };

    private final TargetKind targetKind;
    private final ActionFeature feature;

    ActionType(TargetKind targetKind, ActionFeature feature) {
        this.targetKind = targetKind;
        this.feature = feature;
    }

    public TargetKind getTargetKind() {
        return targetKind;
    }

    public ActionFeature getFeature() {
        return feature;
    }

    public abstract Map<ResourceType, Integer> getCost();

    public abstract int[] getValidTargets(Board board, int playerId);
}
