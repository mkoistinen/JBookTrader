package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.Indicator;
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
        int bids = 0;
        for (MarketDepthItem item : marketDepth.getBids()) {
            bids += item.getSize();
        }

        int asks = 0;
        for (MarketDepthItem item : marketDepth.getAsks()) {
            asks += item.getSize();
        }

        double totalDepth = (bids + asks);

        if (bids > asks) {
            value = bids / totalDepth;
        } else {
            value = -(asks / totalDepth);
        }

        value *= 100;
        return value;
    }
}
