package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;

public class PriceEMA extends Indicator {
    private final double multiplier;

    public PriceEMA(int length) {
        multiplier = 2. / (length + 1.);
    }

    @Override
    public double calculate() {
        double price = marketBook.getLastMarketSnapshot().getMidPrice();
        value += (price - value) * multiplier;

        return value;
    }

}
