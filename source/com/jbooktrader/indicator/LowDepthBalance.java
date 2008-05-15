package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 *
 */
public class LowDepthBalance extends Indicator {

    public LowDepthBalance(MarketBook marketBook) {
        super(marketBook);
    }

    @Override
    public double calculate() {
        value = marketBook.getLastMarketDepth().getLowBalance();
        return value;
    }
}
