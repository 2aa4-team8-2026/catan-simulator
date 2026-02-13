package genaicodebase.board;

public class Board {
    private Tile[] tiles;
    private Node[] nodes;
    private Edge[] edges;
    public Tile[] tile;
    public Node[] node;
    public Edge[] edge;

    public Board(Tile[] tiles, Node[] nodes, Edge[] edges) {
        this.tiles = tiles;
        this.nodes = nodes;
        this.edges = edges;
        this.tile = tiles;
        this.node = nodes;
        this.edge = edges;
    }

    public Board(Tile[] tiles) {
        this.tiles = tiles;
        this.tile = tiles;
        this.nodes = null;
        this.edges = null;
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public void setNodes(Node[] nodes) {
        this.nodes = nodes;
    }

    public Edge[] getEdges() {
        return edges;
    }

    public void setEdges(Edge[] edges) {
        this.edges = edges;
    }

    public Tile getTile(int id) {
        Tile[] source = tile != null ? tile : tiles;
        if (source == null) {
            return null;
        }
        for (Tile t : source) {
            if (t != null && t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public Node getNode(int id) {
        if (node != null) {
            for (Node n : node) {
                if (n != null && n.getId() == id) {
                    return n;
                }
            }
        }
        if (nodes == null) {
            return null;
        }
        for (Node node : nodes) {
            if (node != null && node.getId() == id) {
                return node;
            }
        }
        return null;
    }

    public Edge getEdge(int id) {
        if (edge != null) {
            for (Edge e : edge) {
                if (e != null && e.getId() == id) {
                    return e;
                }
            }
        }
        if (edges == null) {
            return null;
        }
        for (Edge edge : edges) {
            if (edge != null && edge.getId() == id) {
                return edge;
            }
        }
        return null;
    }
}
