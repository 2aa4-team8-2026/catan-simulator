package team8.catan.logging;

public class RoadPlacement {
    private final int edgeId;
    private final int ownerPlayerId;

    public RoadPlacement(int edgeId, int ownerPlayerId) {
        this.edgeId = edgeId;
        this.ownerPlayerId = ownerPlayerId;
    }

    public int getEdgeId() {
        return edgeId;
    }

    public int getOwnerPlayerId() {
        return ownerPlayerId;
    }
}
