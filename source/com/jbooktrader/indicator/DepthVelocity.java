package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 *
 */
public class DepthVelocity extends Indicator {
    private final int period;

    public DepthVelocity(MarketBook marketBook, int period) {
        super(marketBook);
        this.period = period;
    }

    public double calculate() {
        int indexNow = marketBook.size() - 1;
        int indexThen = indexNow - period;
        value = getDepthBalance(indexNow) - getDepthBalance(indexThen);
        return value;
    }


    public double getDepthBalance(int timeIndex) {
        MarketDepth marketDepth = marketBook.getMarketDepth(timeIndex);
        int bids = marketDepth.getCumulativeBidSize();
        int asks = marketDepth.getCumulativeAskSize();
        double totalDepth = (bids + asks);
        value = 100. * (bids - asks) / totalDepth;
        return value;
    }
}
