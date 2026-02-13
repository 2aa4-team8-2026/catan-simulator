package genaicodebase.board;

public class MapPreset {
    public Board createDefaultBoard() {
        return new Board(new Tile[0], new Node[0], new Edge[0]);
    }
}
