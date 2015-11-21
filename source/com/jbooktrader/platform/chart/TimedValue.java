package com.jbooktrader.platform.chart;

/**
 * A container holding time and the value.
 *
 * @author Eugene Kononov
 */
public class TimedValue {
    private final long time;
    private final double value;

    public TimedValue(long time, double value) {
        this.time = time;
        this.value = value;
    }

    public long getTime() {
        return time;
    }

    public double getValue() {
        return value;
    }
}
