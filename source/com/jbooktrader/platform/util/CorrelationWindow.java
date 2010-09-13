package com.jbooktrader.platform.util;

public class CorrelationWindow {
    private class Pair {
        public final double value1, value2;

        private Pair(double value1, double value2) {
            this.value1 = value1;
            this.value2 = value2;
        }
    }

    private final Pair[] buffer;
    private double sum1, sum1Squared, sum2, sum2Squared, sumProduct;
    private int start, end;
    private boolean isFull;

    public CorrelationWindow(int size) {
        buffer = new Pair[size];
    }

    public void add(double value1, double value2) {
        sum1 += value1;
        sum1Squared += (value1 * value1);
        sum2 += value2;
        sum2Squared += (value2 * value2);
        sumProduct += (value1 * value2);


        Pair oldestPair = buffer[end];
        double oldest1 = oldestPair.value1;
        double oldest2 = oldestPair.value2;

        sum1 -= oldest1;
        sum1Squared -= (oldest1 * oldest1);
        sum2 -= oldest2;
        sum2Squared -= (oldest2 * oldest2);
        sumProduct -= (oldest1 * oldest2);


        buffer[end] = new Pair(value1, value2);
        end = (end + 1) % buffer.length;
        if (end == start) {
            start = (start + 1) % buffer.length;
            isFull = true;
        }
    }

    public boolean isFull() {
        return isFull;
    }

    public double getCorrelation() {
        double correlation = 0;
        double numerator = buffer.length * sumProduct - sum1 * sum2;
        double denominator = Math.sqrt(buffer.length * sum1Squared - sum1 * sum1) * Math.sqrt(buffer.length * sum2Squared - sum2 * sum2);

        if (denominator != 0) {
            correlation = 100 * (numerator / denominator);
        }
        return correlation;
    }

    public void clear() {
        isFull = false;
        start = end = 0;
        sum1 = sum1Squared = sum2 = sum2Squared = sumProduct = 0;
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = new Pair(0, 0);
        }
    }
}
