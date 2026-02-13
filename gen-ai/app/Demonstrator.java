package genaicodebase.app;

import genaicodebase.board.Board;
import genaicodebase.board.MapPreset;
import genaicodebase.configuration.SimulationConfig;
import genaicodebase.gameplay.Game;
import genaicodebase.output.ConsoleActionLogger;
import genaicodebase.players.Player;
import genaicodebase.players.RandomPolicy;
import genaicodebase.players.ResourceHand;
import genaicodebase.rules.RuleChecker;

public class Demonstrator {
    public static void main(String[] args) {
        MapPreset preset = new MapPreset();
        Board board = preset.createDefaultBoard();
        SimulationConfig config = new SimulationConfig(10);
        Player[] players = new Player[] {
            new Player(1, 0, new ResourceHand(new int[0]), new RandomPolicy()),
            new Player(2, 0, new ResourceHand(new int[0]), new RandomPolicy())
        };
        Game game = new Game(board, new ConsoleActionLogger(), new RuleChecker(), players, config);
        game.run();
    }
}
