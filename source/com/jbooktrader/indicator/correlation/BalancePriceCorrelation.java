package com.jbooktrader.indicator.correlation;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.util.movingwindow.*;


/**
 *
 */
public class BalancePriceCorrelation extends Indicator {
    private final MovingWindowCorrelation window;

    public BalancePriceCorrelation(int periodLength) {
        window = new MovingWindowCorrelation(periodLength);
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

