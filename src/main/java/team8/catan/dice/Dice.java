package team8.catan.dice;

import java.util.Objects;
import java.util.Random;

public class Dice {
    private static final int DEFAULT_MIN = 1;
    private static final int DEFAULT_MAX = 6;

    private final int min;
    private final int max;
    private final Random random;

    public Dice() {
        this(DEFAULT_MIN, DEFAULT_MAX);
    }

    public Dice(int min, int max) {
        this(min, max, new Random());
    }

    public Dice(int min, int max, Random random) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }

        this.min = min;
        this.max = max;
        this.random = Objects.requireNonNull(random, "random");
    }

    public int roll() {
        return random.nextInt(max - min + 1) + min;
    }
}
