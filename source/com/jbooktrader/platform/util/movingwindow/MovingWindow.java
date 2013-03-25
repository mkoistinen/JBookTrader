package com.jbooktrader.platform.util.movingwindow;

/**
 * @author Eugene Kononov
 */
public class MovingWindow {
    protected final double[] elements;
    protected int start, end;
    protected boolean isFull;
    protected final int capacity;

    public MovingWindow(int capacity) {
        this.capacity = capacity;
        elements = new double[capacity];
    }

    public int getCapacity() {
        return capacity;
    }

    public double get(int position) {
        int index = (start + position - 1) % capacity;
        if (index < 0) {
            index = capacity + index;
        }

        return elements[index];
    }

    public double getFirst() {
        return get(0);
    }

    public double getLast() {
        return get(capacity - 1);
    }

    public void add(double value) {
        elements[end] = value;
        end = (end + 1) % capacity;
        if (end == start) {
            start = (start + 1) % capacity;
            isFull = true;
        }
    }

    public boolean isFull() {
        return isFull;
    }

    public void clear() {
        isFull = false;
        start = end = 0;
        for (int index = 0; index < capacity; index++) {
            elements[index] = 0;
        }
    }
}
