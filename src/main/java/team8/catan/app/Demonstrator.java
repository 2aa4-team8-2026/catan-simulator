package team8.catan.app;

import team8.catan.gameplay.Game;
import team8.catan.gameplay.GameFactory;

import java.io.IOException;
import java.nio.file.Path;

public class Demonstrator {
    public static void main(String[] args) {
        String configPath = args.length > 0 ? args[0] : "team8/catan/config/game-config.json";

        try {
            Game game = new GameFactory().createGame(Path.of(configPath));
            game.run();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load game config from: " + configPath, ex);
        }
    }
}
