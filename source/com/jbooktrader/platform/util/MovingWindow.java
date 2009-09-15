package com.jbooktrader.platform.util;

public class MovingWindow {
    private final double[] buffer;
    private double sum, sumSquared;
    private int start, end;
    private boolean isFull;

    public MovingWindow(int size) {
        buffer = new double[size];
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

    public double getSum() {
        return sum;
    }

    public double getMean() {
        return sum / buffer.length;
    }

    public double getSumSquared() {
        return sumSquared;
    }

    public double getStdev() {
        return Math.sqrt((sumSquared - (sum * sum) / buffer.length) / buffer.length);
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
