package team8.catan.actions;

import team8.catan.board.ResourceType;
import team8.catan.board.StructureType;

import java.util.Map;

public enum ActionType {
    BUILD_ROAD(TargetKind.EDGE, ActionFeature.CORE) {
        @Override
        public Map<ResourceType, Integer> getCost() {
            return StructureType.ROAD.cost();
        }

        @Override
        public String describe(int targetId) {
            return "build a road on edge " + targetId;
        }
    },
    BUILD_SETTLEMENT(TargetKind.NODE, ActionFeature.CORE) {
        @Override
        public Map<ResourceType, Integer> getCost() {
            return StructureType.SETTLEMENT.cost();
        }

        @Override
        public String describe(int targetId) {
            return "build a settlement on node " + targetId;
        }
    },
    BUILD_CITY(TargetKind.NODE, ActionFeature.CORE) {
        @Override
        public Map<ResourceType, Integer> getCost() {
            return StructureType.CITY.cost();
        }

        @Override
        public String describe(int targetId) {
            return "build a city on node " + targetId;
        }
    },
    PASS(TargetKind.NONE, ActionFeature.CORE) {
        @Override
        public Map<ResourceType, Integer> getCost() {
            return Map.of();
        }

        @Override
        public String describe(int targetId) {
            return "pass";
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

    public abstract String describe(int targetId);
}
