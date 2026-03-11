package team8.catan.players;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;

public class ConsoleHumanInputPort implements HumanInputPort {
    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleHumanInputPort(Scanner scanner, PrintStream out) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.out = Objects.requireNonNull(out, "out");
    }

    @Override
    public String readLine(String prompt) {
        out.print(prompt);
        return scanner.nextLine();
    }

    @Override
    public void printLine(String message) {
        out.println(message);
    }
}
