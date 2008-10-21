package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;

/**
 * Mid depth balance for the last 1-second bar
 */
public class DepthBalance extends Indicator {

    @Override
    public void calculate() {
        value = marketBook.getLastMarketSnapshot().getBalance();
    }
}
