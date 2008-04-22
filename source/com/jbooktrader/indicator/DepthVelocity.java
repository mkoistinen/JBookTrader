package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 *
 */
public class DepthVelocity extends Indicator {
    private final int period;

    public DepthVelocity(MarketBook marketBook, int period) {
        super(marketBook);
        this.period = period;
    }

    public double calculate() {
        int indexNow = marketBook.size() - 1;
        int indexThen = indexNow - period + 1;
        value = getDepthBalance(indexNow) - getDepthBalance(indexThen);
        return value;
    }


    private double getDepthBalance(int timeIndex) {
        return marketBook.getMarketDepth(timeIndex).getBalance();
    }
}
