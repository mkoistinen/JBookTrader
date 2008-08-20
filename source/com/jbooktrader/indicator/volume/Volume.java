package com.jbooktrader.indicator.volume;

import com.jbooktrader.platform.indicator.*;

/**
 * Mid depth balance for the last 1-second bar
 */
public class Volume extends Indicator {

    public Volume() {
    }

    @Override
    public double calculate() {
        value = marketBook.getLastMarketDepth().getVolume();
        return value;
    }
}
