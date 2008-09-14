package com.jbooktrader.platform.indicator;

import com.jbooktrader.platform.util.*;

import java.util.*;

/**
 *
 */
public class ChartableIndicator {
    private final Indicator indicator;
    private final String name;
    private final List<TimedValue> indicatorValues;

    public ChartableIndicator(Indicator indicator) {
        this.indicator = indicator;
        name = indicator.getClass().getSimpleName();
        indicatorValues = new ArrayList<TimedValue>();
    }

    public String getName() {
        return name;
    }

    public Indicator getIndicator() {
        return indicator;
    }

    public List<TimedValue> getIndicatorHistory() {
        return indicatorValues;
    }

    public void add(long time, double value) {
        indicatorValues.add(new TimedValue(time, value));
    }

}

/* $Id$ */
