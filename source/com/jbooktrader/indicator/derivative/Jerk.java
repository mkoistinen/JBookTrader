package com.jbooktrader.indicator.derivative;

import com.jbooktrader.platform.indicator.*;

/**
 *
 */
public class Jerk extends Indicator {
    private final int halfPeriod;

    public Jerk(Indicator parentIndicator, int halfPeriod) {
        super(parentIndicator);
        this.halfPeriod = halfPeriod;
    }

    private double acceleration(int indicatorBarIndex) {
        IndicatorBarHistory indicatorBarHistory = parentIndicator.getIndicatorBarHistory();
        double valueNow = indicatorBarHistory.getIndicatorBar(indicatorBarIndex).getClose();
        double valueHalfPeriodAgo = indicatorBarHistory.getIndicatorBar(indicatorBarIndex - halfPeriod).getClose();
        double valueTwoHalfPeriodsAgo = indicatorBarHistory.getIndicatorBar(indicatorBarIndex - 2 * halfPeriod).getClose();
        value = (valueNow - 2 * valueHalfPeriodAgo + valueTwoHalfPeriodsAgo) / (2 * halfPeriod);
        return value;
    }


    @Override
    public double calculate() {
        int lastIndex = parentIndicator.getIndicatorBarHistory().size() - 1;
        double valueNow = acceleration(lastIndex);
        double valueHalfPeriodAgo = acceleration(lastIndex - halfPeriod);
        double valueTwoHalfPeriodsAgo = acceleration(lastIndex - 2 * halfPeriod);
        value = (valueNow - 2 * valueHalfPeriodAgo + valueTwoHalfPeriodsAgo) / (2 * halfPeriod);
        return value;
    }
}
