package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;


/**
 * Relative Strength Index. Implemented up to this specification:
 * http://en.wikipedia.org/wiki/Relative_strength
 */
public class PriceRSI extends Indicator {
    private final int periodLength;

    public PriceRSI(MarketBook marketBook, int periodLength) {
        super(marketBook);
        this.periodLength = periodLength;
    }

    @Override
    public double calculate() {
        //todo: cache it
        int lastIndex = marketBook.size() - 1;
        int firstIndex = lastIndex - periodLength + 1;

        double gains = 0, losses = 0;

        for (int index = firstIndex + 1; index <= lastIndex; index++) {
            double change = marketBook.getMarketDepth(index).getMidPrice() - marketBook.getMarketDepth(index - 1).getMidPrice();
            gains += Math.max(0, change);
            losses += Math.max(0, -change);
        }

        double change = gains + losses;

        value = (change == 0) ? 50 : (100 * gains / change);
        return value;

    }
}
