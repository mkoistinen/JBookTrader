package com.jbooktrader.platform.indicator;


public class IndicatorValue {
    private final long time;
    private final double value;

    public IndicatorValue(long time, double value) {
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
