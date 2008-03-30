package com.jbooktrader.platform.indicator;

/**
 *
 */
public class ChartableIndicator {
    private final Indicator indicator;
    private final String name;

    public ChartableIndicator(String name, Indicator indicator) {
        this.name = name;
        this.indicator = indicator;
    }

    public String getName() {
        return name;
    }

    public Indicator getIndicator() {
        return indicator;
    }

}
