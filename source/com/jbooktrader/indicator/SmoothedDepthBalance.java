package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.marketdepth.*;

public class SmoothedDepthBalance extends Indicator {
    private final int length;

    public SmoothedDepthBalance(MarketBook marketBook, int length) {
        super(marketBook);
        this.length = length;
    }

    @Override
    public double calculate() {
        int size = marketBook.size();
        double average = 0;
        for (int time = size - length; time < size; time++) {
            MarketDepth marketDepth = marketBook.getMarketDepth(time);
            int bids = 0;
            for (MarketDepthItem item : marketDepth.getBids()) {
                bids += item.getSize();
            }

            int asks = 0;
            for (MarketDepthItem item : marketDepth.getAsks()) {
                asks += item.getSize();
            }

            double totalDepth = (bids + asks);
            average += 100. * (bids - asks) / totalDepth;
        }

        value = average / length;
        return value;
    }
}
