package com.jbooktrader.platform.trader;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

public class ResubscriberRunner implements Runnable {
    public void run() {
        Dispatcher dispatcher = Dispatcher.getInstance();
        try {
            Thread.sleep(60 * 60 * 1000); // 60 minutes
            TraderAssistant traderAssistant = dispatcher.getTrader().getAssistant();
            if (!traderAssistant.getIsMarketDataActive()) {
                Collection<Strategy> strategies = traderAssistant.getAllStrategies();
                synchronized (strategies) {
                    for (Strategy strategy : strategies) {
                        traderAssistant.cancelMarketData(strategy);
                    }
                    for (Strategy strategy : strategies) {
                        traderAssistant.requestMarketData(strategy);
                    }
                }
            }
        } catch (Throwable t) {
            dispatcher.getEventReport().report(t);
        }
    }
}

