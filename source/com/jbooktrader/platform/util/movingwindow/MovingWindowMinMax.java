package com.jbooktrader.platform.util.movingwindow;

/**
 * @author Eugene Kononov
 */
public class MovingWindowMinMax extends MovingWindow {
    private double min, max;
    private boolean isInitialized;

    public MovingWindowMinMax(int capacity) {
        super(capacity);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    public void add(double value) {

        if (!isInitialized) {
            min = max = value;
            isInitialized = true;
        }

        double oldestElement = elements[end];

        elements[end] = value;
        end = (end + 1) % capacity;

        if (value > max) {
            max = value;
        } else if (value < min) {
            min = value;
        }

        if (oldestElement == max || oldestElement == min) {
            updateMinMax();
        }

        if (end == start) {
            start = (start + 1) % capacity;
            isFull = true;
        }
    }

    private void updateMinMax() {
        min = max = elements[0];
        int capacity = getCapacity();
        for (int index = 1; index < capacity; index++) {
            double element = elements[index];
            if (element > max) {
                max = element;
            } else if (element < min) {
                min = element;
            }
        }
    }
}
