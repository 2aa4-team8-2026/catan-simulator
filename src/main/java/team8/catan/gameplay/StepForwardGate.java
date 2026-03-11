package team8.catan.gameplay;

import team8.catan.players.Player;

public interface StepForwardGate {
    void awaitGo(int round, Player player, GamePhase phase);
}
