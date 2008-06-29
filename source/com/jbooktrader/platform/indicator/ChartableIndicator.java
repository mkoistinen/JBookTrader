package com.jbooktrader.platform.indicator;

import java.util.*;

/**
 *
 */
public class ChartableIndicator {
    private final Indicator indicator;
    private final String name;
    private final List<IndicatorValue> indicatorValues;

    public ChartableIndicator(String name, Indicator indicator) {
        this.name = name;
        this.indicator = indicator;
        indicatorValues = new ArrayList<IndicatorValue>();
    }

    public String getName() {
        return name;
    }

    public Indicator getIndicator() {
        return indicator;
    }

    public List<IndicatorValue> getIndicatorHistory() {
        return indicatorValues;
    }

    public void add(long time, double value) {
        indicatorValues.add(new IndicatorValue(time, value));
    }

}
