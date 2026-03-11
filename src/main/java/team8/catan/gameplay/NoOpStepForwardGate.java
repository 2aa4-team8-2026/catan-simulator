package team8.catan.gameplay;

import team8.catan.players.Player;

public class NoOpStepForwardGate implements StepForwardGate {
    @Override
    public void awaitGo(int round, Player player, GamePhase phase) {
        // No-op by design for non-human player
    }
}
