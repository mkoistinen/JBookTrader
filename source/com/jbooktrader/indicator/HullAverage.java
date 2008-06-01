package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;


/**
 */
public class HullAverage extends Indicator {
    private final int periodLength;

    public HullAverage(MarketBook marketBook, int periodLength) {
        super(marketBook);
        this.periodLength = periodLength;
    }


    @Override
    public double calculate() {
        int lastBar = marketBook.size() - 1;
        int firstBar = lastBar - periodLength + 1;

        double ave = 0;
        for (int bar = firstBar; bar <= lastBar; bar++) {
            ave += marketBook.getMarketDepth(bar).getMidBalance();
        }
        ave = ave / (lastBar - firstBar + 1);

        double halfAve = 0;
        firstBar = lastBar - periodLength / 2;
        for (int bar = firstBar; bar <= lastBar; bar++) {
            halfAve += marketBook.getMarketDepth(bar).getMidBalance();
        }
        halfAve = halfAve / (lastBar - firstBar + 1);

        value = 2 * halfAve - ave;
        return value;
    }
}
