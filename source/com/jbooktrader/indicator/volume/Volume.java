package com.jbooktrader.indicator.volume;

import com.jbooktrader.platform.indicator.*;

/**
 * Volume for the last period
 */
public class Volume extends Indicator {

    public Volume() {
    }

    @Override
    public double calculate() {
        value = marketBook.getLastMarketSnapshot().getVolume();
        return value;
    }
}
