package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.marketdepth.*;

/**
 *
 */
public class TruePrice extends Indicator {

    public TruePrice(MarketBook marketBook) {
        super(marketBook);
    }

    @Override
    public double calculate() {
        MarketDepth marketDepth = marketBook.getLastMarketDepth();
        int bids = 0;
        double weightedBid = 0;
        for (MarketDepthItem item : marketDepth.getBids()) {
            int size = item.getSize();
            bids += size;
            weightedBid += size * item.getPrice();
        }
        weightedBid /= bids;

        int asks = 0;
        double weightedAsk = 0;
        for (MarketDepthItem item : marketDepth.getAsks()) {
            int size = item.getSize();
            asks += size;
            weightedAsk += size * item.getPrice();
        }
        weightedAsk /= asks;

        value = (weightedBid + weightedAsk) / 2;

        return value;
    }
}
