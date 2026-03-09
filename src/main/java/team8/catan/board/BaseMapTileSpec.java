package team8.catan.board;

public class BaseMapTileSpec {
    private final int q;
    private final int s;
    private final int r;
    private final ResourceType resourceType;
    private final Integer numberToken;

    public BaseMapTileSpec(int q, int s, int r, ResourceType resourceType, Integer numberToken) {
        this.q = q;
        this.s = s;
        this.r = r;
        this.resourceType = resourceType;
        this.numberToken = numberToken;
    }

    int getQ() {
        return q;
    }

    int getS() {
        return s;
    }

    int getR() {
        return r;
    }

    ResourceType getResourceType() {
        return resourceType;
    }

    Integer getNumberToken() {
        return numberToken;
    }
}
