package genaicodebase.board;

public class Tile {
    private final int id;
    private int diceNumber;
    private int[] adjacentNodeIds;
    private ResourceType resourceType;

    public Tile(int id, int diceNumber, int[] adjacentNodeIds, ResourceType resourceType) {
        this.id = id;
        this.diceNumber = diceNumber;
        this.adjacentNodeIds = adjacentNodeIds;
        this.resourceType = resourceType;
    }

    public int getId() {
        return id;
    }

    public int getDiceNumber() {
        return diceNumber;
    }

    public void setDiceNumber(int diceNumber) {
        this.diceNumber = diceNumber;
    }

    public int[] getAdjacentNodeIds() {
        return adjacentNodeIds;
    }

    public void setAdjacentNodeIds(int[] adjacentNodeIds) {
        this.adjacentNodeIds = adjacentNodeIds;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }
}
