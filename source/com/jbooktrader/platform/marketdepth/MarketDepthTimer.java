package com.jbooktrader.platform.marketdepth;

import java.util.*;
import java.util.concurrent.*;

public class MarketDepthTimer {
    private static final long PERIOD = 60000000; // 60 ms
    private final List<MarketBook> marketBooks;
    private static MarketDepthTimer instance;

    class Signaller implements Runnable {
        public void run() {
            synchronized (marketBooks) {
                for (MarketBook marketBook : marketBooks) {
                    marketBook.signal();
                }
            }
        }
    }

    synchronized public static MarketDepthTimer getInstance() {
        if (instance == null) {
            instance = new MarketDepthTimer();
        }
        return instance;
    }

    private MarketDepthTimer() {
        marketBooks = new ArrayList<MarketBook>();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new Signaller(), 0, PERIOD, TimeUnit.NANOSECONDS);
    }

    public void addListener(MarketBook marketBook) {
        synchronized (marketBooks) {
            marketBooks.add(marketBook);
        }
    }

}

