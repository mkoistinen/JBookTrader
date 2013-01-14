package com.jbooktrader.platform.indicator;


import com.jbooktrader.platform.marketbook.*;

/**
 * Base class for all classes implementing technical indicators.
 *
 * @author Eugene Kononov
 */
public abstract class Indicator {
    private final String key;
    protected MarketBook marketBook;
    protected double value;

    public abstract void calculate();

    public abstract void reset();

    protected Indicator(int... parameters) {
        String name = getClass().getSimpleName();
        if (parameters.length == 0) {
            throw new RuntimeException("No parameters passed from the constructor of indicator " + name);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        for (int parameter : parameters) {
            if (sb.length() > name.length() + 1) {
                sb.append(",");
            }
            sb.append(parameter);
        }
        sb.append(")");
        key = sb.toString();
    }

    public String getKey() {
        return key;
    }

    public void setMarketBook(MarketBook marketBook) {
        this.marketBook = marketBook;
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
