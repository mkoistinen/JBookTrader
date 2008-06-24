package com.jbooktrader.indicator.derivative;

import com.jbooktrader.platform.indicator.*;

/**
 *
 */
public class Strength extends Indicator {
    private final int period;

    public Strength(Indicator parentIndicator, int period) {
        super(parentIndicator);
        this.period = period;
    }

    @Override
    public double calculate() {
        IndicatorBarHistory indicatorBarHistory = parentIndicator.getIndicatorBarHistory();
        int lastIndex = indicatorBarHistory.size() - 1;
        int firstIndex = lastIndex - period + 1;

        double gains = 0, losses = 0;

        for (int bar = firstIndex + 1; bar <= lastIndex; bar++) {
            double now = indicatorBarHistory.getIndicatorBar(bar).getClose();
            double then = indicatorBarHistory.getIndicatorBar(bar - 1).getClose();
            double change = now - then;
            gains += Math.max(0, change);
            losses += Math.max(0, -change);
        }

        double change = gains + losses;

        value = (change == 0) ? 0 : (100 * (gains / change) - 50);
        return value;

    }
}
