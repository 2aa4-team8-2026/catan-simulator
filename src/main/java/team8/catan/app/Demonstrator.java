package app;

import gameplay.Game;
import gameplay.GameFactory;
import players.Player;

import java.io.IOException;
import java.nio.file.Path;

public final class Demonstrator {
    public static void main(String[] args) {
        String configPath = args.length > 0 ? args[0] : "catan/config/game-config.json";

        try {
            Game game = new GameFactory().createGame(Path.of(configPath));
            game.run();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load game config from: " + configPath, ex);
        }

        // todo: move end game logging to Game class
//        System.out.println("Rounds played: " + game.getRound());
//        for (Player player : game.getPlayers()) {
//            System.out.println("Player " + player.getId() + " VP: " + player.getVictoryPoints());
//        }
    }
}
