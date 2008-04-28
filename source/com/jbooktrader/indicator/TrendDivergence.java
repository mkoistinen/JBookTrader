package com.jbooktrader.indicator;

import com.jbooktrader.platform.bar.*;
import com.jbooktrader.platform.indicator.*;

/**
 *
 */
public class TrendDivergence extends Indicator {
    private final int shorter, longer;

    public TrendDivergence(PriceHistory priceHistory, int shorter, int longer) {
        super(priceHistory);
        this.shorter = shorter;
        this.longer = longer;
    }

    @Override
    public double calculate() {
        double shorterTrend = calculate(shorter);
        double longerTrend = calculate(longer);
        value = longerTrend - shorterTrend;
        return value;
    }


    public double calculate(int period) {

        int lastIndex = priceHistory.size() - 1;
        int firstIndex = lastIndex - period + 1;

        double meanX = 0, meanY = 0;
        for (int index = firstIndex; index <= lastIndex; index++) {
            meanX += index;
            meanY += priceHistory.getPriceBar(index).getClose();
        }
        meanX /= period;
        meanY /= period;

        double residualXSquared = 0, sum = 0;
        for (int index = firstIndex; index <= lastIndex; index++) {
            double y = priceHistory.getPriceBar(index).getClose();
            double residualX = index - meanX;
            sum += residualX * (y - meanY);
            residualXSquared += residualX * residualX;
        }

        return period * (sum / residualXSquared);

    }
}
