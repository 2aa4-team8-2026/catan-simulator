package dice;

public class TwoDice extends Dice {
    public TwoDice() {
        super();
    }

    public TwoDice(int min, int max) {
        super(min, max);
    }

    @Override
    public int roll() {
        return super.roll() + super.roll();
    }
}
