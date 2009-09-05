package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;

import java.util.*;

public class PriceSMA extends Indicator {
    private final int period;
    private final LinkedList<Double> prices;
    private double sum;

    public PriceSMA(int period) {
        this.period = period;
        prices = new LinkedList<Double>();
    }

    @Override
    public void calculate() {

        double price = marketBook.getSnapshot().getPrice();
        sum += price;

        // In with the new
        prices.add(price);

        // Out with the old
        while (prices.size() > period) {
            sum -= prices.removeFirst();
        }

        if (!prices.isEmpty()) {
            value = sum / prices.size();
        }
    }

    @Override
    public void reset() {
        value = sum = 0.0;
        prices.clear();
    }
}
