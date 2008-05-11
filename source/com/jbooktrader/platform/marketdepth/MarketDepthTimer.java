package com.jbooktrader.platform.marketdepth;

import com.jbooktrader.platform.model.*;

import java.util.*;
import java.util.concurrent.*;

public class MarketDepthTimer {
    private static final long PERIOD = 25000000; // 25 ms
    private final List<MarketBook> marketBooks;
    private static MarketDepthTimer instance;


    class Signaller implements Runnable {
        public void run() {
            try {
                synchronized (marketBooks) {
                    for (MarketBook marketBook : marketBooks) {
                        marketBook.signal();
                    }
                }
            } catch (Exception e) {
                Dispatcher.getReporter().report(e);
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

