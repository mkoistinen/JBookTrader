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
    private final IndicatorBarHistory indicatorBarHistory;
    protected final Indicator parentIndicator;
    private int type;


    public abstract double calculate();


    private Indicator(MarketBook marketBook, PriceHistory priceHistory, Indicator parentIndicator) {
        this.marketBook = marketBook;
        this.priceHistory = priceHistory;
        this.parentIndicator = parentIndicator;
        indicatorBarHistory = new IndicatorBarHistory();
    }


    protected Indicator(PriceHistory priceHistory) {
        this(null, priceHistory, null);
        type = 1;
    }


    protected Indicator(MarketBook marketBook) {
        this(marketBook, null, null);
        type = 0;
    }

    protected Indicator(Indicator parentIndicator) {
        this(null, null, parentIndicator);
        type = 1;
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

    public IndicatorBarHistory getIndicatorBarHistory() {
        return indicatorBarHistory;
    }
}
