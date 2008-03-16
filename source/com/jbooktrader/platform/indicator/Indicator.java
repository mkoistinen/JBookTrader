package com.jbooktrader.platform.indicator;


import com.jbooktrader.platform.marketdepth.MarketBook;

import java.util.LinkedList;

/**
 * Base class for all classes implementing technical indicators.
 */
public abstract class Indicator {
    protected double value;
    protected MarketBook marketBook;
    private final LinkedList<IndicatorValue> history;

    public abstract double calculate();

    public Indicator(MarketBook marketBook) {
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

    public long getTime() {
        return marketBook.getLastMarketDepth().getTime();
    }

    public void addToHistory(long date, double value) {
        history.add(new IndicatorValue(date, value));
    }

    public LinkedList<IndicatorValue> getHistory() {
        return history;
    }


}
