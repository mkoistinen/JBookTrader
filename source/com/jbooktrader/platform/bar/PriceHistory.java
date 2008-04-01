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
        double bid = marketDepth.getBestBid();
        double ask = marketDepth.getBestAsk();
        double midPoint = (bid + ask) / 2;
        long time = marketDepth.getTime();


        long frequency = 60 * 1000;
        // Integer division gives us the number of whole periods
        long completedPeriods = time / frequency;
        long barTime = (completedPeriods + 1) * frequency;

        if (bar == null) {
            bar = new PriceBar(barTime, midPoint);
        }

        if (barTime > bar.getTime()) {
            priceBars.add(bar);
            bar = new PriceBar(barTime, midPoint);
        }

        bar.setClose(midPoint);
        bar.setLow(Math.min(bid, bar.getLow()));
        bar.setHigh(Math.max(ask, bar.getHigh()));
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
