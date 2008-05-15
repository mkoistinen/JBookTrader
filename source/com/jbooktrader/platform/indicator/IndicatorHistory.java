package com.jbooktrader.platform.indicator;

import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;

import java.util.*;

/**
 * Holds and validates the priceBar history for a strategy.
 */
public class IndicatorHistory {
    private static final int MAX_SIZE = 24 * 60;
    private final LinkedList<IndicatorBar> indicatorBars;
    private IndicatorBar indicatorBar;

    public IndicatorHistory() {
        indicatorBars = new LinkedList<IndicatorBar>();
    }

    public LinkedList<IndicatorBar> getAll() {
        return indicatorBars;
    }

    public synchronized void update(long time, double price) {

        long frequency = 60 * 1000;
        // Integer division gives us the number of whole periods
        long completedPeriods = time / frequency;
        long barTime = (completedPeriods + 1) * frequency;

        if (indicatorBar == null) {
            indicatorBar = new IndicatorBar(barTime, price);
        }

        if (barTime > indicatorBar.getTime()) {
            indicatorBars.add(indicatorBar);
            indicatorBar = new IndicatorBar(barTime, price);
            if (Dispatcher.getMode() == Optimization) {
                if (indicatorBars.size() > MAX_SIZE) {
                    indicatorBars.removeFirst();
                }
            }
        }

        indicatorBar.setClose(price);
        indicatorBar.setLow(Math.min(price, indicatorBar.getLow()));
        indicatorBar.setHigh(Math.max(price, indicatorBar.getHigh()));
    }

    public int size() {
        return indicatorBars.size();
    }

    public IndicatorBar getIndicatorBar(int index) {
        return indicatorBars.get(index);
    }

    public int getSize() {
        return indicatorBars.size();
    }

    public IndicatorBar getLastPriceBar() {
        return indicatorBars.getLast();
    }

    public IndicatorBar getFirstPriceBar() {
        return indicatorBars.getFirst();
    }
}
