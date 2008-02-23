package com.jbooktrader.platform.indicator;


import com.jbooktrader.platform.marketdepth.MarketBook;

import java.util.*;

/**
 * Base class for all classes implementing technical indicators.
 */
public abstract class Indicator {
    protected double value;
    protected MarketBook marketBook;
    private final List<IndicatorValue> history;
    protected Indicator parent;

    public abstract double calculate();// must be implemented in subclasses.

    public Indicator() {
        history = new ArrayList<IndicatorValue>();
    }


    public Indicator(MarketBook marketBook) {
        this();
        this.marketBook = marketBook;
    }

    public Indicator(Indicator parent) {
        this();
        this.parent = parent;
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
        if (marketBook != null) {
            return marketBook.getLastMarketDepth().getTime();
        } else {
            List<IndicatorValue> parentHistory = parent.getHistory();
            return parentHistory.get(parentHistory.size() - 1).getTime();
        }
    }


    public void addToHistory(long date, double value) {
        history.add(new IndicatorValue(date, value));
    }

    public List<IndicatorValue> getHistory() {
        return history;
    }


}
