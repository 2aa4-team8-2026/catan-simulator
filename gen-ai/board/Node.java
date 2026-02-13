package genaicodebase.board;

public class Node {
    private final int id;
    private int ownerId;
    private StructureType structureType;

    public Node(int id, int ownerId, StructureType structureType) {
        this.id = id;
        this.ownerId = ownerId;
        this.structureType = structureType;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public StructureType getStructureType() {
        return structureType;
    }

    public void setStructureType(StructureType structureType) {
        this.structureType = structureType;
    }
}
