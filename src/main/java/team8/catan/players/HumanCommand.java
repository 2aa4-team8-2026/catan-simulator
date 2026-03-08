package team8.catan.players;

public final class HumanCommand {
    public enum Type {
        ROLL,
        GO,
        LIST,
        SHOW_ACTIONS,
        BUILD_MENU,
        BUILD_SETTLEMENT,
        BUILD_CITY,
        BUILD_ROAD,
        INVALID
    }

    private final Type type;
    private final Integer nodeId;
    private final Integer fromNodeId;
    private final Integer toNodeId;
    private final String error;

    private HumanCommand(Type type, Integer nodeId, Integer fromNodeId, Integer toNodeId, String error) {
        this.type = type;
        this.nodeId = nodeId;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.error = error;
    }

    public static HumanCommand roll() {
        return new HumanCommand(Type.ROLL, null, null, null, null);
    }

    public static HumanCommand go() {
        return new HumanCommand(Type.GO, null, null, null, null);
    }

    public static HumanCommand list() {
        return new HumanCommand(Type.LIST, null, null, null, null);
    }

    public static HumanCommand showActions() {
        return new HumanCommand(Type.SHOW_ACTIONS, null, null, null, null);
    }

    public static HumanCommand buildMenu() {
        return new HumanCommand(Type.BUILD_MENU, null, null, null, null);
    }

    public static HumanCommand buildSettlement(int nodeId) {
        return new HumanCommand(Type.BUILD_SETTLEMENT, nodeId, null, null, null);
    }

    public static HumanCommand buildCity(int nodeId) {
        return new HumanCommand(Type.BUILD_CITY, nodeId, null, null, null);
    }

    public static HumanCommand buildRoad(int fromNodeId, int toNodeId) {
        return new HumanCommand(Type.BUILD_ROAD, null, fromNodeId, toNodeId, null);
    }

    public static HumanCommand invalid(String error) {
        return new HumanCommand(Type.INVALID, null, null, null, error);
    }

    public Type getType() {
        return type;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public Integer getFromNodeId() {
        return fromNodeId;
    }

    public Integer getToNodeId() {
        return toNodeId;
    }

    public String getError() {
        return error;
    }
}
