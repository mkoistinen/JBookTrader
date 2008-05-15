package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 *
 */
public class HighDepthBalance extends Indicator {

    public HighDepthBalance(MarketBook marketBook) {
        super(marketBook);
    }

    @Override
    public double calculate() {
        value = marketBook.getLastMarketDepth().getHighBalance();
        return value;
    }
}
