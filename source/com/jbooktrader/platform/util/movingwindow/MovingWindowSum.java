package com.jbooktrader.platform.util.movingwindow;

/**
 * @author Eugene Kononov
 */
public class MovingWindowSum extends MovingWindow {
    private double sum;

    public MovingWindowSum(int size) {
        super(size);
    }

    @Override
    public void add(double value) {
        sum = sum + value - getFirst();
        super.add(value);
    }

    public double getSum() {
        return sum;
    }

    @Override
    public void clear() {
        sum = 0;
        super.clear();
    }
}