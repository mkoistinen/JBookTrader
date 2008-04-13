package com.jbooktrader.platform.marketdepth;

import java.util.concurrent.*;

public class MarketDepthTimer {
    private static final long PERIOD = 50000000; // 50 ms

    public MarketDepthTimer(final MarketBook marketBook) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable signaller = new Runnable() {
            public void run() {
                marketBook.signal();
            }
        };

        scheduler.scheduleWithFixedDelay(signaller, 0, PERIOD, TimeUnit.NANOSECONDS);
    }
}

