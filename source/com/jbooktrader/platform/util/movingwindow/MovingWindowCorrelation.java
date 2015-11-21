package com.jbooktrader.platform.util.movingwindow;

/**
 * @author Eugene Kononov
 */
public class MovingWindowCorrelation {
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

    public MovingWindowCorrelation(int capacity) {
        buffer = new Pair[capacity];
        for (int i = 0; i < capacity; i++) {
            buffer[i] = new Pair(0, 0);
        }

    }

    private Pair get(int position) {
        int index = (start + position - 1) % buffer.length;
        if (index < 0) {
            index = buffer.length + index;
        }

        return buffer[index];
    }

    public void add(double value1, double value2) {
        sum1 += value1;
        sum1Squared += (value1 * value1);
        sum2 += value2;
        sum2Squared += (value2 * value2);
        sumProduct += (value1 * value2);


        Pair firstPair = get(0);
        double first1 = firstPair.value1;
        double first2 = firstPair.value2;

        sum1 -= first1;
        sum1Squared -= (first1 * first1);
        sum2 -= first2;
        sum2Squared -= (first2 * first2);
        sumProduct -= (first1 * first2);


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
