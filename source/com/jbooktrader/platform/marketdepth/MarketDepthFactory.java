package com.jbooktrader.platform.marketdepth;

import java.util.*;

public class MarketDepthFactory extends TimerTask {
    private final MarketBook marketBook;

    public MarketDepthFactory(MarketBook marketBook, long frequency) {
        this.marketBook = marketBook;
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(this, 0, frequency);
    }

    @Override
    public void run() {
        marketBook.signal();
    }
}

