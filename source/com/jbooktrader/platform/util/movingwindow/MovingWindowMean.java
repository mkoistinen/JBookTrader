package com.jbooktrader.platform.util.movingwindow;

/**
 * @author Eugene Kononov
 */
public class MovingWindowMean extends MovingWindow {
    private double sum;

    public MovingWindowMean(int size) {
        super(size);
    }

    @Override
    public void add(double value) {
        sum = sum + value - getFirst();
        super.add(value);
    }

    public double getMean() {
        return sum / getCapacity();
    }

    @Override
    public void clear() {
        sum = 0;
        super.clear();
    }
}
