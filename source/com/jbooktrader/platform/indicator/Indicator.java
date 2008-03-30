package com.jbooktrader.platform.indicator;


import com.jbooktrader.platform.bar.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 * Base class for all classes implementing technical indicators.
 */
public abstract class Indicator {
    protected double value;
    protected final MarketBook marketBook;
    protected final PriceHistory priceHistory;
    private final IndicatorHistory indicatorHistory;
    private int type;


    public abstract double calculate();


    private Indicator(MarketBook marketBook, PriceHistory priceHistory) {
        this.marketBook = marketBook;
        this.priceHistory = priceHistory;
        indicatorHistory = new IndicatorHistory();
    }


    protected Indicator(PriceHistory priceHistory) {
        this(null, priceHistory);
        type = 1;
    }


    protected Indicator(MarketBook marketBook) {
        this(marketBook, null);
        type = 0;
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

    public int getType() {
        return type;
    }

    public IndicatorHistory getBarHistory() {
        return indicatorHistory;
    }
}
