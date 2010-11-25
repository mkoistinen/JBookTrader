package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;

public class PriceEMA extends Indicator {

    private final double alpha;

    public PriceEMA(int period) {
        super(period);
        alpha = 2.0 / (period + 1.0);
    }

    @Override
    public void reset() {
        value = 0.0;
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        value += alpha * (price - value);
    }

}
