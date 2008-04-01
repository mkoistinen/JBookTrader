package com.jbooktrader.indicator;

import com.jbooktrader.platform.bar.*;
import com.jbooktrader.platform.indicator.*;


/**
 * Relative Strength Index. Implemented up to this specification:
 * http://en.wikipedia.org/wiki/Relative_strength
 */
public class RSI extends Indicator {
    private final int periodLength;

    public RSI(PriceHistory priceHistory, int periodLength) {
        super(priceHistory);
        this.periodLength = periodLength;
    }

    @Override
    public double calculate() {
        int lastBar = priceHistory.size() - 1;
        int firstBar = lastBar - periodLength + 1;

        double gains = 0, losses = 0;

        for (int bar = firstBar + 1; bar <= lastBar; bar++) {
            double change = priceHistory.getPriceBar(bar).getClose() - priceHistory.getPriceBar(bar - 1).getClose();
            gains += Math.max(0, change);
            losses += Math.max(0, -change);
        }

        double change = gains + losses;

        value = (change == 0) ? 50 : (100 * gains / change);
        return value;
    }
}
