package com.jbooktrader.platform.bar;

import java.util.*;

/**
 * Holds and validates the priceBar history for a strategy.
 */
public class PriceHistory {
    private final LinkedList<PriceBar> priceBars;
    private PriceBar bar;

    public PriceHistory() {
        priceBars = new LinkedList<PriceBar>();
    }

    public LinkedList<PriceBar> getAll() {
        return priceBars;
    }

    public synchronized void update(long time, double price) {
        long frequency = 60 * 1000;
        // Integer division gives us the number of whole periods
        long completedPeriods = time / frequency;
        long barTime = (completedPeriods + 1) * frequency;

        if (bar == null) {
            bar = new PriceBar(barTime, price);
        }

        if (barTime > bar.getTime()) {
            priceBars.add(bar);
            bar = new PriceBar(barTime, price);
        }

        bar.setClose(price);
        bar.setLow(Math.min(price, bar.getLow()));
        bar.setHigh(Math.max(price, bar.getHigh()));
    }

    public int size() {
        return priceBars.size();
    }

    public PriceBar getPriceBar(int index) {
        return priceBars.get(index);
    }

    public int getSize() {
        return priceBars.size();
    }

    public PriceBar getLastPriceBar() {
        return priceBars.getLast();
    }

    public PriceBar getFirstPriceBar() {
        return priceBars.getFirst();
    }
}
