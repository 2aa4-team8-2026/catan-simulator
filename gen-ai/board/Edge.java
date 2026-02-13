package genaicodebase.board;

public class Edge {
    private final int id;
    private Node nodeA;
    private Node nodeB;
    private int roadOwnerId;

    public Edge(int id, int roadOwnerId, Node nodeA, Node nodeB) {
        this.id = id;
        this.roadOwnerId = roadOwnerId;
        this.nodeA = nodeA;
        this.nodeB = nodeB;
    }

    public int getId() {
        return id;
    }

    public int getRoadOwnerId() {
        return roadOwnerId;
    }

    public void setRoadOwnerId(int roadOwnerId) {
        this.roadOwnerId = roadOwnerId;
    }

    public Node getNodeA() {
        return nodeA;
    }

    public void setNodeA(Node nodeA) {
        this.nodeA = nodeA;
    }

    public Node getNodeB() {
        return nodeB;
    }

    public void setNodeB(Node nodeB) {
        this.nodeB = nodeB;
    }
}
