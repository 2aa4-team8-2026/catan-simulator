package team8.catan.output;

import team8.catan.board.Board;
import team8.catan.players.Player;

import java.util.List;

public interface GameStateWriter {
    void write(Board board, List<Player> players);
}
