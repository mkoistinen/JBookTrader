package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;

/**
 * Mid depth depth for the last 1-second bar
 */
public class DepthBalance extends Indicator {

    public DepthBalance() {
    }

    @Override
    public double calculate() {
        value = marketBook.getLastMarketSnapshot().getMidBalance();
        return value;
    }
}
