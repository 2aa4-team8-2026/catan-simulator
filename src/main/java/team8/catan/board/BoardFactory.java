package team8.catan.board;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BoardFactory {
    public Board buildBoard(List<BaseMapTileSpec> tileSpecs) {
        List<LatticePoint> cornerOffsets = List.of(
            new LatticePoint(1, 1),
            new LatticePoint(0, 2),
            new LatticePoint(-1, 1),
            new LatticePoint(-1, -1),
            new LatticePoint(0, -2),
            new LatticePoint(1, -1)
        );

        Map<LatticePoint, Set<LatticePoint>> adjacency = new HashMap<>();
        List<List<LatticePoint>> tileCornersById = new ArrayList<>(tileSpecs.size());

        for (BaseMapTileSpec tileSpec : tileSpecs) {
            LatticePoint center = new LatticePoint(2 * tileSpec.getQ() + tileSpec.getR(), 3 * tileSpec.getR());
            List<LatticePoint> corners = new ArrayList<>(6);
            for (LatticePoint offset : cornerOffsets) {
                LatticePoint corner = center.add(offset);
                corners.add(corner);
                adjacency.computeIfAbsent(corner, ignored -> new HashSet<>());
            }
            tileCornersById.add(corners);

            for (int i = 0; i < corners.size(); i++) {
                LatticePoint a = corners.get(i);
                LatticePoint b = corners.get((i + 1) % corners.size());
                adjacency.get(a).add(b);
                adjacency.get(b).add(a);
            }
        }

        Map<LatticePoint, Integer> nodeIds = assignNodeIdsLikeVisualizer(tileSpecs, tileCornersById);
        List<Node> nodes = new ArrayList<>(54);
        for (int i = 0; i < 54; i++) {
            nodes.add(new Node(i));
        }

        List<int[]> edgeEndpoints = new ArrayList<>();
        for (Map.Entry<LatticePoint, Set<LatticePoint>> entry : adjacency.entrySet()) {
            LatticePoint a = entry.getKey();
            for (LatticePoint b : entry.getValue()) {
                if (comparePoints(a, b) < 0) {
                    Integer idA = nodeIds.get(a);
                    Integer idB = nodeIds.get(b);
                    if (idA == null || idB == null) {
                        throw new IllegalStateException("Missing node mapping for edge.");
                    }
                    edgeEndpoints.add(new int[] { Math.min(idA, idB), Math.max(idA, idB) });
                }
            }
        }

        edgeEndpoints.sort(Comparator.comparingInt((int[] pair) -> pair[0]).thenComparingInt(pair -> pair[1]));
        if (edgeEndpoints.size() != 72) {
            throw new IllegalStateException("Expected 72 edges, found " + edgeEndpoints.size());
        }

        List<Edge> edges = new ArrayList<>(72);
        for (int i = 0; i < edgeEndpoints.size(); i++) {
            int[] pair = edgeEndpoints.get(i);
            edges.add(new Edge(i, pair[0], pair[1]));
        }

        List<Tile> tiles = new ArrayList<>(tileSpecs.size());
        int robberTileId = 0;
        for (int i = 0; i < tileSpecs.size(); i++) {
            BaseMapTileSpec spec = tileSpecs.get(i);
            List<LatticePoint> corners = tileCornersById.get(i);
            int[] adjacentNodes = new int[corners.size()];
            for (int j = 0; j < corners.size(); j++) {
                Integer nodeId = nodeIds.get(corners.get(j));
                if (nodeId == null) {
                    throw new IllegalStateException("Missing node mapping for tile corner.");
                }
                adjacentNodes[j] = nodeId;
            }
            tiles.add(new Tile(
                i,
                spec.getQ(),
                spec.getS(),
                spec.getR(),
                spec.getResourceType(),
                spec.getNumberToken(),
                adjacentNodes
            ));
            if (spec.getResourceType() == null) {
                robberTileId = i;
            }
        }

        return new Board(nodes, edges, tiles, robberTileId);
    }

    private static Map<LatticePoint, Integer> assignNodeIdsLikeVisualizer(
        List<BaseMapTileSpec> tileSpecs,
        List<List<LatticePoint>> tileCornersById
    ) {
        Map<CubePoint, EnumMap<NodeRef, Integer>> tileNodes = new HashMap<>();
        Map<LatticePoint, Integer> nodeIds = new HashMap<>();
        int nextNodeId = 0;

        for (int i = 0; i < tileSpecs.size(); i++) {
            BaseMapTileSpec tileSpec = tileSpecs.get(i);
            CubePoint cubePoint = new CubePoint(tileSpec.getQ(), tileSpec.getS(), tileSpec.getR());
            EnumMap<NodeRef, Integer> nodesForTile = new EnumMap<>(NodeRef.class);

            for (CubeDirection direction : CubeDirection.values()) {
                EnumMap<NodeRef, Integer> neighborNodes = tileNodes.get(cubePoint.add(direction));
                if (neighborNodes != null) {
                    applyNeighborNodeReuse(nodesForTile, neighborNodes, direction);
                }
            }

            for (NodeRef nodeRef : NodeRef.values()) {
                if (!nodesForTile.containsKey(nodeRef)) {
                    nodesForTile.put(nodeRef, nextNodeId++);
                }
            }

            List<LatticePoint> corners = tileCornersById.get(i);
            for (NodeRef nodeRef : NodeRef.values()) {
                LatticePoint corner = corners.get(nodeRef.cornerIndex);
                int nodeId = nodesForTile.get(nodeRef);
                Integer previous = nodeIds.putIfAbsent(corner, nodeId);
                if (previous != null && previous != nodeId) {
                    throw new IllegalStateException(
                        "Inconsistent node id mapping for corner (" + corner.x + ", " + corner.y + ")"
                    );
                }
            }

            tileNodes.put(cubePoint, nodesForTile);
        }

        if (nextNodeId != 54 || nodeIds.size() != 54) {
            throw new IllegalStateException(
                "Expected 54 visualizer-compatible nodes, found ids=" + nextNodeId + ", corners=" + nodeIds.size()
            );
        }

        return nodeIds;
    }

    private static void applyNeighborNodeReuse(
        EnumMap<NodeRef, Integer> current,
        EnumMap<NodeRef, Integer> neighbor,
        CubeDirection direction
    ) {
        switch (direction) {
            case EAST -> {
                current.putIfAbsent(NodeRef.NORTHEAST, neighbor.get(NodeRef.NORTHWEST));
                current.putIfAbsent(NodeRef.SOUTHEAST, neighbor.get(NodeRef.SOUTHWEST));
            }
            case SOUTHEAST -> {
                current.putIfAbsent(NodeRef.SOUTH, neighbor.get(NodeRef.NORTHWEST));
                current.putIfAbsent(NodeRef.SOUTHEAST, neighbor.get(NodeRef.NORTH));
            }
            case SOUTHWEST -> {
                current.putIfAbsent(NodeRef.SOUTH, neighbor.get(NodeRef.NORTHEAST));
                current.putIfAbsent(NodeRef.SOUTHWEST, neighbor.get(NodeRef.NORTH));
            }
            case WEST -> {
                current.putIfAbsent(NodeRef.NORTHWEST, neighbor.get(NodeRef.NORTHEAST));
                current.putIfAbsent(NodeRef.SOUTHWEST, neighbor.get(NodeRef.SOUTHEAST));
            }
            case NORTHWEST -> {
                current.putIfAbsent(NodeRef.NORTH, neighbor.get(NodeRef.SOUTHEAST));
                current.putIfAbsent(NodeRef.NORTHWEST, neighbor.get(NodeRef.SOUTH));
            }
            case NORTHEAST -> {
                current.putIfAbsent(NodeRef.NORTH, neighbor.get(NodeRef.SOUTHWEST));
                current.putIfAbsent(NodeRef.NORTHEAST, neighbor.get(NodeRef.SOUTH));
            }
        }
    }

    private static int comparePoints(LatticePoint a, LatticePoint b) {
        int byX = Integer.compare(a.x, b.x);
        if (byX != 0) {
            return byX;
        }
        return Integer.compare(a.y, b.y);
    }

    private static class LatticePoint {
        private final int x;
        private final int y;

        private LatticePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private LatticePoint add(LatticePoint other) {
            return new LatticePoint(x + other.x, y + other.y);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LatticePoint other)) {
                return false;
            }
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private enum NodeRef {
        NORTH(4),
        NORTHEAST(5),
        SOUTHEAST(0),
        SOUTH(1),
        SOUTHWEST(2),
        NORTHWEST(3);

        private final int cornerIndex;

        NodeRef(int cornerIndex) {
            this.cornerIndex = cornerIndex;
        }
    }

    private enum CubeDirection {
        EAST(1, -1, 0),
        SOUTHEAST(0, -1, 1),
        SOUTHWEST(-1, 0, 1),
        WEST(-1, 1, 0),
        NORTHWEST(0, 1, -1),
        NORTHEAST(1, 0, -1);

        private final int dq;
        private final int ds;
        private final int dr;

        CubeDirection(int dq, int ds, int dr) {
            this.dq = dq;
            this.ds = ds;
            this.dr = dr;
        }
    }

    private static class CubePoint {
        private final int q;
        private final int s;
        private final int r;

        private CubePoint(int q, int s, int r) {
            this.q = q;
            this.s = s;
            this.r = r;
        }

        private CubePoint add(CubeDirection direction) {
            return new CubePoint(q + direction.dq, s + direction.ds, r + direction.dr);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CubePoint other)) {
                return false;
            }
            return q == other.q && s == other.s && r == other.r;
        }

        @Override
        public int hashCode() {
            return Objects.hash(q, s, r);
        }
    }
}
