package com.jbooktrader.platform.marketdepth;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;
import java.util.concurrent.*;

public class StrategyRunner {
    private final List<Strategy> strategies;
    private static StrategyRunner instance;

    class MarketDepthHandler implements Runnable {
        public void run() {
            try {
                synchronized (strategies) {
                    for (Strategy strategy : strategies) {
                        MarketBook marketBook = strategy.getMarketBook();
                        MarketDepth marketDepth = marketBook.getNextMarketDepth();
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

    synchronized public static StrategyRunner getInstance() {
        if (instance == null) {
            instance = new StrategyRunner();
        }
        return instance;
    }

    private StrategyRunner() {
        strategies = new ArrayList<Strategy>();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new MarketDepthHandler(), 0, 1, TimeUnit.SECONDS);
    }

    public void addListener(Strategy strategy) {
        synchronized (strategies) {
            strategies.add(strategy);
        }
    }

}

