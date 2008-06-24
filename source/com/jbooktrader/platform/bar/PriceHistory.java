package com.jbooktrader.platform.bar;

import com.jbooktrader.platform.marketdepth.*;

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

    public synchronized void update(MarketDepth marketDepth) {

        double midPrice = marketDepth.getMidPrice();
        double highPrice = marketDepth.getHighPrice();
        double lowPrice = marketDepth.getLowPrice();
        long time = marketDepth.getTime();

        long frequency = 60 * 1000;
        // Integer division gives us the number of whole periods
        long completedPeriods = time / frequency;
        long barTime = (completedPeriods + 1) * frequency;

        if (bar == null) {
            bar = new PriceBar(barTime, midPrice);
        }

        if (barTime > bar.getTime()) {
            priceBars.add(bar);
            bar = new PriceBar(barTime, midPrice);
        }

        bar.setClose(midPrice);
        bar.setLow(Math.min(lowPrice, bar.getLow()));
        bar.setHigh(Math.max(highPrice, bar.getHigh()));
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
