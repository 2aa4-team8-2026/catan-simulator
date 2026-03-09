package team8.catan.rules;

import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.Node;

final class RoadNetworkService {
    boolean isRoadConnectedToPlayerNetwork(Board board, int edgeId, int playerId) {
        Edge edge = board.getEdge(edgeId);
        if (edge == null) {
            return false;
        }
        return isConnectedAtNode(board, edge, edge.getNodeA(), playerId)
            || isConnectedAtNode(board, edge, edge.getNodeB(), playerId);
    }

    private boolean isConnectedAtNode(Board board, Edge candidate, int nodeId, int playerId) {
        Node node = board.getNode(nodeId);
        if (node == null) {
            return false;
        }
        if (node.getOwnerId() == playerId && node.getStructureType() != null) {
            return true;
        }
        if (node.getStructureType() != null && node.getOwnerId() != playerId) {
            return false;
        }

        for (Edge edge : board.getEdges()) {
            if (edge.getId() == candidate.getId()) {
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
