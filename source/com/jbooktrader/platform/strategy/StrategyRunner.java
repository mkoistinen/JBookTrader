package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.trader.*;
import com.jbooktrader.platform.util.*;

import java.util.*;
import java.util.concurrent.*;

public class StrategyRunner {
    private final Collection<Strategy> strategies;
    private final TraderAssistant traderAssistant;
    private final NTPClock ntpClock;
    private Collection<MarketBook> marketBooks;
    private static StrategyRunner instance;

    class SnapshotHandler implements Runnable {
        public void run() {
            try {
                long ntpTime = ntpClock.getTime();
                long delay = 1000 - ntpTime % 1000;
                Thread.sleep(delay);
                long snapshotTime = ntpTime + delay;
                Dispatcher.fireModelChanged(ModelListener.Event.TimeUpdate, snapshotTime);

                if (marketBooks != null) {
                    for (MarketBook marketBook : marketBooks) {
                        MarketSnapshot marketSnapshot = marketBook.getNextMarketSnapshot(snapshotTime);
                        if (marketSnapshot != null) {
                            marketBook.setSnapshot(marketSnapshot);
                            marketBook.saveSnapshot(marketSnapshot);
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
        ntpClock = new NTPClock();
        traderAssistant = Dispatcher.getTrader().getAssistant();
        strategies = new ArrayList<Strategy>();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new SnapshotHandler(), 0, 500, TimeUnit.MILLISECONDS);
    }

    public void addListener(Strategy strategy) {
        synchronized (strategies) {
            strategies.add(strategy);
            marketBooks = traderAssistant.getAllMarketBooks().values();
        }
    }

}

