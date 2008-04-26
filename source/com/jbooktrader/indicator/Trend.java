package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
//import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.bar.*;

//import java.util.*;

/**
 * Slope of the bar close prices
 */
public class Trend extends Indicator {
    private final int period;

    public Trend(PriceHistory priceHistory, int period) {
        super(priceHistory);
        this.period = period;
    }

    @Override
    public double calculate() {

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
            sum += residualX * (y-meanY);
            residualXSquared += residualX * residualX;
        }

        value = period * (sum /  residualXSquared);
        return value;

    }
}
