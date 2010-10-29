package com.jbooktrader.platform.util;

/**
 * For reference, see "Rapid calculation methods" section in this document:
 * http://en.wikipedia.org/wiki/Standard_deviation
 */
public class MovingWindow {
    private final double[] buffer;
    private double sum, sumSquared;
    private int start, end;
    private boolean isFull;

    public MovingWindow(int size) {
        buffer = new double[size];
    }

    public double getElement(int index) {
        return buffer[index];
    }

    public void add(double value) {
        sum += value;
        sumSquared += (value * value);

        double oldestValue = buffer[end];
        sum -= oldestValue;
        sumSquared -= (oldestValue * oldestValue);

        buffer[end] = value;
        end = (end + 1) % buffer.length;
        if (end == start) {
            start = (start + 1) % buffer.length;
            isFull = true;
        }
    }

    public boolean isFull() {
        return isFull;
    }

    public double getMean() {
        return sum / buffer.length;
    }

    public double getStdev() {
        double num = buffer.length * sumSquared - (sum * sum);
        double denom = buffer.length * (buffer.length - 1);
        return Math.sqrt(num / denom);
    }

    public void clear() {
        isFull = false;
        start = end = 0;
        sum = sumSquared = 0;
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = 0;
        }
    }
}
