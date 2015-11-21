package com.jbooktrader.platform.util.movingwindow;

/**
 * For reference, see "Rapid calculation methods" section in this document:
 * http://en.wikipedia.org/wiki/Standard_deviation
 *
 * @author Eugene Kononov
 */
public class MovingWindowStDev extends MovingWindow {
    private double sum, sumSquared;

    public MovingWindowStDev(int size) {
        super(size);
    }

    @Override
    public void add(double value) {
        double oldest = getFirst();
        sum = sum + value - oldest;
        sumSquared = sumSquared + (value * value) - (oldest * oldest);
        super.add(value);
    }

    public double getMean() {
        return sum / getCapacity();
    }

    public double getStdev() {
        int capacity = getCapacity();
        double num = capacity * sumSquared - (sum * sum);
        double denom = capacity * (capacity - 1);
        return Math.sqrt(num / denom);
    }

    @Override
    public void clear() {
        sum = sumSquared = 0;
        super.clear();
    }
}
