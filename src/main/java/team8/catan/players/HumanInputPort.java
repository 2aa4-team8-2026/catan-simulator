package team8.catan.players;

public interface HumanInputPort {
    String readLine(String prompt);

    void printLine(String message);
}
