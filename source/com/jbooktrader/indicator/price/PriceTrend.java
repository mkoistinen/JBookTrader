package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 * BalanceSlope of the bar close prices
 */
public class PriceTrend extends Indicator {
    private final int period;

    public PriceTrend(MarketBook marketBook, int period) {
        super(marketBook);
        this.period = period;
    }

    @Override
    public double calculate() {
        int lastIndex = marketBook.size() - 1;
        int firstIndex = lastIndex - period + 1;

        double meanX = 0, meanY = 0;
        for (int index = firstIndex; index <= lastIndex; index++) {
            meanX += index;
            meanY += marketBook.getMarketDepth(index).getMidPrice();
        }
        meanX /= period;
        meanY /= period;

        double residualXSquared = 0, sum = 0;
        for (int index = firstIndex; index <= lastIndex; index++) {
            double y = marketBook.getMarketDepth(index).getMidPrice();
            double residualX = index - meanX;
            sum += residualX * (y - meanY);
            residualXSquared += residualX * residualX;
        }

        value = period * (sum / residualXSquared);
        return value;
    }
}
