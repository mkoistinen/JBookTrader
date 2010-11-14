package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;

/**
 * Double-smoothed average of price. 
 * Reference: http://en.wikipedia.org/wiki/Double_exponential_smoothing#Double_exponential_smoothing
 */
public class PriceDoubleEMA extends Indicator {

    private final double alpha, beta;
    double s0, s1, b;

    public PriceDoubleEMA(int period1, int period2) {
        alpha = 2.0 / (period1 + 1.0);
        beta = 2.0 / (period2 + 1.0);
    }

    @Override
    public void reset() {
        value = s0 = marketBook.getSnapshot().getPrice();
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        s1 = alpha * price + (1 - alpha) * value;
        b = beta * (s1 - s0) + (1 - beta) * b;
        s0 = s1;
        value = s1 + b;
    }

}
