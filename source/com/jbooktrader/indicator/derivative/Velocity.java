package com.jbooktrader.indicator.derivative;

import com.jbooktrader.platform.indicator.*;

/**
 *
 */
public class Velocity extends Indicator {
    private final int period;

    public Velocity(Indicator parentIndicator, int period) {
        super(parentIndicator);
        this.period = period;
    }

    @Override
    public double calculate() {
        IndicatorBarHistory indicatorBarHistory = parentIndicator.getIndicatorBarHistory();
        int lastIndex = indicatorBarHistory.size() - 1;
        double valueNow = indicatorBarHistory.getIndicatorBar(lastIndex).getClose();
        double valueOnePeriodAgo = indicatorBarHistory.getIndicatorBar(lastIndex - period).getClose();

        value = (valueNow - valueOnePeriodAgo) / period;
        return value;
    }
}
