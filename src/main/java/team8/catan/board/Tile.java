package team8.catan.board;

import java.util.Arrays;

public final class Tile {
    private final int id;
    private final int q;
    private final int s;
    private final int r;
    private final ResourceType resourceType;
    private final Integer numberToken;
    private final int[] adjacentNodeIds;

    public Tile(
        int id,
        int q,
        int s,
        int r,
        ResourceType resourceType,
        Integer numberToken,
        int[] adjacentNodeIds
    ) {
        this.id = id;
        this.q = q;
        this.s = s;
        this.r = r;
        this.resourceType = resourceType;
        this.numberToken = numberToken;
        this.adjacentNodeIds = Arrays.copyOf(adjacentNodeIds, adjacentNodeIds.length);
    }

    public int getId() {
        return id;
    }

    public int getQ() {
        return q;
    }

    public int getS() {
        return s;
    }

    public int getR() {
        return r;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public Integer getNumberToken() {
        return numberToken;
    }

    public int[] getAdjacentNodeIds() {
        return Arrays.copyOf(adjacentNodeIds, adjacentNodeIds.length);
    }
}
