package team8.catan.output;

import team8.catan.board.Board;
import team8.catan.board.Edge;
import team8.catan.board.Node;
import team8.catan.players.Player;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JsonStateWriter implements GameStateWriter {
    private final Path outputPath;

    public JsonStateWriter(Path outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void write(Board board, List<Player> players) {
        Map<Integer, Player> playersById = new HashMap<>();
        for (Player player : players) {
            playersById.put(player.getId(), player);
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"roads\": [\n");

        List<Edge> roads = board.getEdges();
        boolean firstRoad = true;
        for (Edge edge : roads) {
            if (edge.getRoadOwnerId() == Edge.UNOWNED) {
                continue;
            }
            Player owner = playersById.get(edge.getRoadOwnerId());
            if (owner == null) {
                continue;
            }
            if (!firstRoad) {
                json.append(",\n");
            }
            json.append("    { \"a\": ")
                .append(edge.getNodeA())
                .append(", \"b\": ")
                .append(edge.getNodeB())
                .append(", \"owner\": \"")
                .append(owner.getColor().name())
                .append("\" }");
            firstRoad = false;
        }
        json.append("\n  ],\n");

        json.append("  \"buildings\": [\n");
        List<Node> nodes = board.getNodes();
        boolean firstBuilding = true;
        for (Node node : nodes) {
            if (node.getOwnerId() == Node.UNOWNED || node.getStructureType() == null) {
                continue;
            }
            Player owner = playersById.get(node.getOwnerId());
            if (owner == null) {
                continue;
            }
            if (!firstBuilding) {
                json.append(",\n");
            }
            json.append("    { \"node\": ")
                .append(node.getId())
                .append(", \"owner\": \"")
                .append(owner.getColor().name())
                .append("\", \"type\": \"")
                .append(node.getStructureType().name())
                .append("\" }");
            firstBuilding = false;
        }
        json.append("\n  ]\n");
        json.append("}\n");

        try {
            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(outputPath, json.toString());
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to write state file to " + outputPath, ex);
        }
    }
}
