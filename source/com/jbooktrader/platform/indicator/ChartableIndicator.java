package com.jbooktrader.platform.indicator;

/**
 *
 */
public class ChartableIndicator {
    private final Indicator indicator;
    private final String name;
    private final int chartIndex;

    public ChartableIndicator(String name, Indicator indicator, int chartIndex) {
        this.name = name;
        this.indicator = indicator;
        this.chartIndex = chartIndex;
    }

    public String getName() {
        return name;
    }

    public Indicator getIndicator() {
        return indicator;
    }

    public int getChartIndex() {
        return chartIndex;
    }

    public boolean isEmpty() {
        return indicator.getHistory().size() == 0;
    }

}
