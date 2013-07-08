package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.model.ModelListener.*;
import com.jbooktrader.platform.util.ntp.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class StrategyRunner {
    private final Collection<Strategy> strategies;
    private Collection<MarketBook> marketBooks;
    private final Dispatcher dispatcher;
    private static StrategyRunner instance;
    private static long snapshotTime;
    private static final long ONE_SECOND = 1000;
    private static final long HALF_SECOND = ONE_SECOND / 2;

    private class SnapshotHandler implements Runnable {
        public void run() {
            NTPClock ntpClock = dispatcher.getNTPClock();
            long ntpTime;

            while (true) {
                try {
                    while ((ntpTime = ntpClock.getTime()) < snapshotTime) {
                        Thread.sleep(HALF_SECOND);
                    }

                    long delay = ONE_SECOND - ntpTime % ONE_SECOND;
                    Thread.sleep(delay);
                    snapshotTime = ntpTime + delay;

                    synchronized (strategies) {    //ekk need to take another look to see if sync is needed on this level
                        if (marketBooks != null) {
                            dispatcher.fireModelChanged(Event.TimeUpdate, snapshotTime);

                            for (MarketBook marketBook : marketBooks) {
                                marketBook.takeMarketSnapshot(snapshotTime);
                            }

                            for (Strategy strategy : strategies) {
                                strategy.process();
                            }
                        }
                    }

                } catch (Throwable t) {
                    dispatcher.getEventReport().report(t);
                }
            }
        }
    }

    public static synchronized StrategyRunner getInstance() {
        if (instance == null) {
            instance = new StrategyRunner();
        }
        return instance;
    }

    private StrategyRunner() {
        dispatcher = Dispatcher.getInstance();
        strategies = new LinkedList<>();
        (new Thread(new SnapshotHandler())).start();
    }

    public void addListener(Strategy strategy) {
        synchronized (strategies) {
            strategies.add(strategy);
            marketBooks = dispatcher.getTrader().getAssistant().getAllMarketBooks().values();
        }
    }

}

