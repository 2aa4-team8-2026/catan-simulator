package team8.catan.gameplay;

import team8.catan.players.HumanInputPort;
import team8.catan.players.Player;

import java.util.Objects;

public class ConsoleStepForwardGate implements StepForwardGate {
    private final HumanInputPort inputPort;

    public ConsoleStepForwardGate(HumanInputPort inputPort) {
        this.inputPort = Objects.requireNonNull(inputPort, "inputPort");
    }

    @Override
    public void awaitGo(int round, Player player, GamePhase phase) {
        while (true) {
            String input = inputPort.readLine(
                "Press Enter (or type go) to proceed to "
                    + phase
                    + " for P"
                    + player.getId()
                    + " (round "
                    + round
                    + "): "
            );
            String normalized = input == null ? "" : input.trim();
            if (normalized.isEmpty() || normalized.equalsIgnoreCase("go") || normalized.equalsIgnoreCase("g")) {
                return;
            }
            inputPort.printLine("Invalid input. Press Enter or type go.");
        }
    }
}
