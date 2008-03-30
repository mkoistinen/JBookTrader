package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 *
 */
public class DepthBalance extends Indicator {

    public DepthBalance(MarketBook marketBook) {
        super(marketBook);
    }

    @Override
    public double calculate() {
        MarketDepth marketDepth = marketBook.getLastMarketDepth();
        int bids = marketDepth.getCumulativeBidSize();
        int asks = marketDepth.getCumulativeAskSize();

        double totalDepth = (bids + asks);
        value = 100. * (bids - asks) / totalDepth;

        return value;
    }
}
