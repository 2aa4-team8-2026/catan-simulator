package team8.catan.board;

public class Edge {
    public static final int UNOWNED = -1;

    private final int id;
    private final int nodeA;
    private final int nodeB;
    private int roadOwnerId;

    public Edge(int id, int nodeA, int nodeB) {
        this.id = id;
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.roadOwnerId = UNOWNED;
    }

    public int getId() {
        return id;
    }

    public int getNodeA() {
        return nodeA;
    }

    public int getNodeB() {
        return nodeB;
    }

    public int getRoadOwnerId() {
        return roadOwnerId;
    }

    public boolean isUnowned() {
        return roadOwnerId == UNOWNED;
    }

    public void placeRoad(int playerId) {
        this.roadOwnerId = playerId;
    }

    public void restoreRoadOwner(int roadOwnerId) {
        this.roadOwnerId = roadOwnerId;
    }
}
