package com.jbooktrader.indicator.derivative;

import com.jbooktrader.platform.indicator.*;

/**
 *
 */
public class Acceleration extends Indicator {
    private final int halfPeriod;

    public Acceleration(Indicator parentIndicator, int halfPeriod) {
        super(parentIndicator);
        this.halfPeriod = halfPeriod;
    }

    @Override
    public double calculate() {
        IndicatorBarHistory indicatorBarHistory = parentIndicator.getIndicatorBarHistory();
        int lastIndex = indicatorBarHistory.size() - 1;
        double valueNow = indicatorBarHistory.getIndicatorBar(lastIndex).getClose();
        double valueHalfPeriodAgo = indicatorBarHistory.getIndicatorBar(lastIndex - halfPeriod).getClose();
        double valueTwoHalfPeriodsAgo = indicatorBarHistory.getIndicatorBar(lastIndex - 2 * halfPeriod).getClose();
        value = (valueNow - 2 * valueHalfPeriodAgo + valueTwoHalfPeriodsAgo) / (2 * halfPeriod);
        return value;
    }
}
