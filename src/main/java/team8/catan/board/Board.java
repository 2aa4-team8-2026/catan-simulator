package team8.catan.board;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Board {
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final List<Tile> tiles;
    private int robberTileId;

    public Board(List<Node> nodes, List<Edge> edges) {
        this(nodes, edges, List.of(), -1);
    }

    public Board(List<Node> nodes, List<Edge> edges, List<Tile> tiles, int robberTileId) {
        this.nodes = new ArrayList<>(nodes);
        this.edges = new ArrayList<>(edges);
        this.tiles = new ArrayList<>(tiles);
        this.robberTileId = robberTileId;
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

    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }

    public Tile getTile(int tileId) {
        if (tileId < 0 || tileId >= tiles.size()) {
            return null;
        }
        return tiles.get(tileId);
    }

    public List<Tile> getTiles() {
        return new ArrayList<>(tiles);
    }

    public int getRobberTileId() {
        return robberTileId;
    }

    public void placeRobber(int tileId) {
        if (tileId < 0 || tileId >= tiles.size()) {
            throw new IllegalArgumentException("Invalid tile id: " + tileId);
        }
        robberTileId = tileId;
    }

    public void restoreRobberTileId(int tileId) {
        robberTileId = tileId;
    }

    public int getRandomTileId(Random random) {
        if (tiles.isEmpty()) {
            return -1;
        }
        return random.nextInt(tiles.size());
    }

    public int getEdgeIdBetweenNodes(int nodeA, int nodeB) {
        for (Edge edge : edges) {
            boolean direct = edge.getNodeA() == nodeA && edge.getNodeB() == nodeB;
            boolean reverse = edge.getNodeA() == nodeB && edge.getNodeB() == nodeA;
            if (direct || reverse) {
                return edge.getId();
            }
        }
        return -1;
    }

    public int[] getAdjacentNodeIdsForTile(int tileId) {
        Tile tile = getTile(tileId);
        if (tile == null) {
            return new int[0];
        }
        return tile.getAdjacentNodeIds();
    }

    public boolean hasStructureAdjacentToTile(int tileId, int playerId) {
        int[] nodeIds = getAdjacentNodeIdsForTile(tileId);
        for (int nodeId : nodeIds) {
            Node node = getNode(nodeId);
            if (node == null) {
                continue;
            }
            if (node.getOwnerId() == playerId && node.getStructureType() != null) {
                return true;
            }
        }
        return false;
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

    public List<Integer> getTileIdsAdjacentToNode(int nodeId) {
        List<Integer> out = new ArrayList<>();
        for (Tile tile : tiles) {
            for (int adjacentNodeId : tile.getAdjacentNodeIds()) {
                if (adjacentNodeId == nodeId) {
                    out.add(tile.getId());
                    break;
                }
            }
        }
        return out;
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
}
