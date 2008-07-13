package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;


/**
 * Relative Strength Index. Implemented up to this specification:
 * http://en.wikipedia.org/wiki/Relative_strength
 */
public class PriceRSI extends Indicator {
    private final double multiplier;
    private double emaUp, emaDown;
    private double previousPrice;

    public PriceRSI(MarketBook marketBook, int periodLength) {
        super(marketBook);
        multiplier = 2. / (periodLength + 1.);
    }

    @Override
    public double calculate() {
        double price = marketBook.getLastMarketDepth().getMidPrice();
        if (previousPrice != 0) {
            double change = price - previousPrice;
            double up = (change > 0) ? change : 0;
            double down = (change < 0) ? -change : 0;
            emaUp += (up - emaUp) * multiplier;
            emaDown += (down - emaDown) * multiplier;
            double sum = emaUp + emaDown;
            value = (sum == 0) ? 50 : (100 * emaUp / sum);
        } else {
            value = 50;
        }
        previousPrice = price;

        return value;
    }
}
