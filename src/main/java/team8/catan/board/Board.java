package team8.catan.board;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Board {
    private final List<Node> nodes;
    private final List<Edge> edges;

    public Board(List<Node> nodes, List<Edge> edges) {
        this.nodes = new ArrayList<>(nodes);
        this.edges = new ArrayList<>(edges);
    }

    public Node getNode(int id) {
        if (id < 0 || id >= nodes.size()) {
            return null;
        }
        return nodes.get(id);
    }

    public Edge getEdge(int id) {
        if (id < 0 || id >= edges.size()) {
            return null;
        }
        return edges.get(id);
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodes);
    }

    public int[] getValidRoadTargets(int playerId) {
        List<Integer> valid = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.isUnowned() && isRoadConnectedToPlayerNetwork(edge.getId(), playerId)) {
                valid.add(edge.getId());
            }
        }
        return toIntArray(valid);
    }

    public int[] getValidSettlementTargets(int playerId) {
        List<Integer> valid = new ArrayList<>();
        for (Node node : nodes) {
            if (node.isUnowned() && node.getStructureType() == null) {
                valid.add(node.getId());
            }
        }
        return toIntArray(valid);
    }

    public int[] getValidCityTargets(int playerId) {
        List<Integer> valid = new ArrayList<>();
        for (Node node : nodes) {
            if (node.getOwnerId() == playerId && node.getStructureType() == StructureType.SETTLEMENT) {
                valid.add(node.getId());
            }
        }
        return toIntArray(valid);
    }

    private int[] toIntArray(List<Integer> values) {
        int[] out = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i);
        }
        return out;
    }

    public List<Integer> getIncidentEdgeIds(int nodeId) {
        List<Integer> incident = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getNodeA() == nodeId || edge.getNodeB() == nodeId) {
                incident.add(edge.getId());
            }
        }
        return incident;
    }

    public List<Integer> getAdjacentNodeIds(int nodeId) {
        Set<Integer> adjacent = new LinkedHashSet<>();
        for (Edge edge : edges) {
            if (edge.getNodeA() == nodeId) {
                adjacent.add(edge.getNodeB());
            } else if (edge.getNodeB() == nodeId) {
                adjacent.add(edge.getNodeA());
            }
        }
        return new ArrayList<>(adjacent);
    }

    public boolean hasAdjacentStructure(int nodeId) {
        for (int adjacentNodeId : getAdjacentNodeIds(nodeId)) {
            Node node = getNode(adjacentNodeId);
            if (node != null && node.getStructureType() != null) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIncidentRoadOwnedBy(int nodeId, int playerId) {
        for (int edgeId : getIncidentEdgeIds(nodeId)) {
            Edge edge = getEdge(edgeId);
            if (edge != null && edge.getRoadOwnerId() == playerId) {
                return true;
            }
        }
        return false;
    }

    public boolean isRoadConnectedToPlayerNetwork(int edgeId, int playerId) {
        Edge edge = getEdge(edgeId);
        if (edge == null) {
            return false;
        }
        return isConnectedToPlayerNetwork(edge, playerId);
    }

    private boolean isConnectedToPlayerNetwork(Edge candidate, int playerId) {
        return isConnectedAtNode(candidate, candidate.getNodeA(), playerId)
            || isConnectedAtNode(candidate, candidate.getNodeB(), playerId);
    }

    private boolean isConnectedAtNode(Edge candidate, int nodeId, int playerId) {
        Node node = getNode(nodeId);
        if (node == null) {
            return false;
        }

        // Direct connection to player's own settlement/city.
        if (node.getOwnerId() == playerId && node.getStructureType() != null) {
            return true;
        }

        // No road can pass through an opponent's structure.
        if (node.getStructureType() != null && node.getOwnerId() != playerId) {
            return false;
        }

        // Connected to any adjacent road owned by the player.
        for (Edge edge : edges) {
            if (edge == candidate) {
                continue;
            }
            if (edge.getRoadOwnerId() != playerId) {
                continue;
            }
            if (edge.getNodeA() == nodeId || edge.getNodeB() == nodeId) {
                return true;
            }
        }

        return false;
    }
}
