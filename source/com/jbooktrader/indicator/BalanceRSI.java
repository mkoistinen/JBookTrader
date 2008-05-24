package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;


/**
 * Relative Strength Index. Implemented up to this specification:
 * http://en.wikipedia.org/wiki/Relative_strength
 */
public class BalanceRSI extends Indicator {
    private final int periodLength;

    public BalanceRSI(MarketBook marketBook, int periodLength) {
        super(marketBook);
        this.periodLength = periodLength;
    }


    @Override
    public double calculate() {
        int lastBar = marketBook.size() - 1;
        int firstBar = lastBar - periodLength + 1;

        double gains = 0, losses = 0;

        for (int bar = firstBar + 1; bar <= lastBar; bar++) {
            double now = marketBook.getMarketDepth(bar).getMidBalance();
            double then = marketBook.getMarketDepth(bar - 1).getMidBalance();
            double change = now - then;
            gains += Math.max(0, change);
            losses += Math.max(0, -change);
        }

        double change = gains + losses;

        value = (change == 0) ? 50 : (100 * gains / change);
        return value;
    }
}
