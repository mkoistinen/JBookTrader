package com.jbooktrader.platform.marketdepth;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;
import java.util.concurrent.*;

public class MarketDepthTimer {
    private static final long PERIOD = 25000000; // 25 ms
    private final List<Strategy> strategies;
    private static MarketDepthTimer instance;


    class MarketDepthHandler implements Runnable {
        public void run() {
            try {
                synchronized (strategies) {
                    for (Strategy strategy : strategies) {
                        MarketBook marketBook = strategy.getMarketBook();
                        MarketDepth marketDepth = marketBook.getNewMarketDepth();
                        if (marketDepth != null) {
                            marketBook.add(marketDepth);
                            strategy.process(marketDepth);
                        }
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
        strategies = new ArrayList<Strategy>();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new MarketDepthHandler(), 0, PERIOD, TimeUnit.NANOSECONDS);
    }

    public void addListener(Strategy strategy) {
        synchronized (strategies) {
            strategies.add(strategy);
        }
    }

}

