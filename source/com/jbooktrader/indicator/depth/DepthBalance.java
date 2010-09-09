package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;

/**
 * Depth balance of the limit order book in the latest market snapshot
 */
public class DepthBalance extends Indicator {

    @Override
    public void calculate() {
        value = marketBook.getSnapshot().getBalance();
    }

    @Override
    public void reset() {
        calculate();
    }

}
