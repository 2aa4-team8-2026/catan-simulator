package team8.catan.board;

public class Node {
    public static final int UNOWNED = -1;

    private final int id;
    private int ownerId;
    private StructureType structureType;

    public Node(int id) {
        this.id = id;
        this.ownerId = UNOWNED;
        this.structureType = null;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public StructureType getStructureType() {
        return structureType;
    }

    public boolean isUnowned() {
        return ownerId == UNOWNED;
    }

    public void placeSettlement(int playerId) {
        this.ownerId = playerId;
        this.structureType = StructureType.SETTLEMENT;
    }

    public void upgradeToCity() {
        this.structureType = StructureType.CITY;
    }
}
