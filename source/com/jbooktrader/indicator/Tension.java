package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 */
public class Tension extends Indicator {
    private final int period;

    public Tension(MarketBook marketBook, int period) {
        super(marketBook);
        this.period = period;
    }

    @Override
    public double calculate() {
        int lastIndex = marketBook.size() - 1;
        int firstIndex = lastIndex - period + 1;

        int bids = 0, offers = 0;
        for (int index = firstIndex; index <= lastIndex; index++) {
            bids += marketBook.getMarketDepth(index).getCumulativeBidSize();
            offers += marketBook.getMarketDepth(index).getCumulativeAskSize();
        }

        double totalDepth = bids + offers;
        int balance = (int) (100. * (bids - offers) / totalDepth);

        double priceChange = marketBook.getMarketDepth(lastIndex).getMidPoint() - marketBook.getMarketDepth(firstIndex).getMidPoint();
        //double tension = 0;
        double tension = priceChange * 10 - balance;

        value = tension;
        return value;

    }
}
