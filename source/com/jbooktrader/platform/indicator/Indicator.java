package com.jbooktrader.platform.indicator;


import com.jbooktrader.platform.marketdepth.*;

/**
 * Base class for all classes implementing technical indicators.
 */
public abstract class Indicator {
    protected final MarketBook marketBook;
    protected final Indicator parentIndicator;
    protected double value;

    public abstract double calculate();

    private Indicator(MarketBook marketBook, Indicator parentIndicator) {
        this.marketBook = marketBook;
        this.parentIndicator = parentIndicator;
    }

    protected Indicator(MarketBook marketBook) {
        this(marketBook, null);
    }

    protected Indicator(Indicator parentIndicator) {
        this(null, parentIndicator);
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

}
