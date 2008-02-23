package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.marketdepth.*;

/**
 *
 */
public class MarketDepthRatio extends Indicator {

    public MarketDepthRatio(MarketBook marketBook) {
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

        value = ((double) bids) / asks;
        return value;
    }
}
