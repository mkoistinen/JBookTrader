package com.jbooktrader.platform.indicator;


import com.jbooktrader.platform.marketdepth.MarketBook;

import java.util.LinkedList;

/**
 * Base class for all classes implementing technical indicators.
 */
public abstract class Indicator {
    protected double value;
    protected final MarketBook marketBook;
    private final LinkedList<IndicatorValue> history;

    public abstract double calculate();

    protected Indicator(MarketBook marketBook) {
        this.marketBook = marketBook;
        history = new LinkedList<IndicatorValue>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" value: ").append(value);
        return sb.toString();
    }

    public double getValue() {
        return value;
    }

    public void addToHistory() {
        history.add(new IndicatorValue(marketBook.getLastMarketDepth().getTime(), value));
    }

    public LinkedList<IndicatorValue> getHistory() {
        return history;
    }


}
