package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.util.*;


/**
 *
 */
public class DepthPriceCorrelation extends Indicator {
    private final CorrelationWindow window;

    public DepthPriceCorrelation(int periodLength) {
        window = new CorrelationWindow(periodLength);
    }

    @Override
    public void calculate() {
        MarketSnapshot snapshot = marketBook.getSnapshot();
        window.add(snapshot.getBalance(), snapshot.getPrice());
        if (window.isFull()) {
            value = window.getCorrelation();
        }
    }

    @Override
    public void reset() {
        window.clear();
        value = 0;
    }

}

