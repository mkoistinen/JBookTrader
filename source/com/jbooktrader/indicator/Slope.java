package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

//import java.util.*;

/**
 * Slope of any indicator
 */
public class Slope extends Indicator {
    private final int period;
    private static long counter;

    public Slope(MarketBook marketBook, int period) {
        super(marketBook);
        this.period = period;
    }

    @Override
    public double calculate() {
        counter++;
        int lastIndex = marketBook.size() - 1;
        int firstIndex = lastIndex - period + 1;

        double meanX = 0, meanY = 0;
        for (int x = firstIndex; x <= lastIndex; x++) {
            meanX += x;
            meanY += marketBook.getMarketDepth(x).getBalance();
        }
        meanX /= period;
        meanY /= period;

        double residualXSquared = 0, sum = 0;
        for (int x = firstIndex; x <= lastIndex; x++) {
            double y = marketBook.getMarketDepth(x).getBalance();
            double residualX = x - meanX;
            sum += residualX * (y-meanY);
            residualXSquared += residualX * residualX;
        }

        value = period * (sum /  residualXSquared);
        return value;

    }
}
