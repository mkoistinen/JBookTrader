package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.trader.*;

import java.util.*;
import java.util.concurrent.*;

public class StrategyRunner {
    private final List<Strategy> strategies;
    private final TraderAssistant traderAssistant;
    private Collection<MarketBook> marketBooks;
    private static StrategyRunner instance;


    class SnapshotRunner implements Runnable {
        public void run() {
            try {
                if (marketBooks != null) {
                    for (MarketBook marketBook : marketBooks) {
                        long time = System.currentTimeMillis();
                        MarketSnapshot marketSnapshot = marketBook.getNextMarketSnapshot(time);
                        if (marketSnapshot != null) {
                            marketBook.add(marketSnapshot);
                            marketBook.save(marketSnapshot);
                        }
                    }

                    synchronized (strategies) {
                        for (Strategy strategy : strategies) {
                            strategy.process();
                        }
                    }
                }
            } catch (Throwable t) {
                Dispatcher.getReporter().report(t);
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
        traderAssistant = Dispatcher.getTrader().getAssistant();
        strategies = new ArrayList<Strategy>();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new SnapshotRunner(), 0, 1, TimeUnit.SECONDS);
    }

    public void addListener(Strategy strategy) {
        synchronized (strategies) {
            strategies.add(strategy);
            marketBooks = traderAssistant.getAllMarketBooks().values();
        }
    }

}

/* $Id$ */
