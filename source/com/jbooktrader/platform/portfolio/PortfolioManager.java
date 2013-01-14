package com.jbooktrader.platform.portfolio;

import com.jbooktrader.platform.model.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class PortfolioManager {
    private static final long MAX_SHORT_STRATEGIES = 7;
    private static final long MAX_LONG_STRATEGIES = 7;
    private final Set<String> shortStrategies;
    private final Set<String> longStrategies;

    public PortfolioManager() {
        shortStrategies = new HashSet<String>();
        longStrategies = new HashSet<String>();
    }

    public void update(String strategyName, int currentPosition) {
        if (currentPosition == 0) {
            shortStrategies.remove(strategyName);
            longStrategies.remove(strategyName);
        }
        if (currentPosition > 0) {
            longStrategies.add(strategyName);
        }
        if (currentPosition < 0) {
            shortStrategies.add(strategyName);
        }

        String s = "Long strategies: " + longStrategies + ", " + "Short strategies: " + shortStrategies;
        Dispatcher.getInstance().getEventReport().report("Portfolio Manager", s);
    }

    public boolean canTrade(String strategyName, String action) {
        if (action.equals("BUY")) {
            if (shortStrategies.contains(strategyName)) {
                return true;
            } else {
                return longStrategies.size() < MAX_LONG_STRATEGIES;
            }
        }

        if (action.equals("SELL")) {
            if (longStrategies.contains(strategyName)) {
                return true;
            } else {
                return shortStrategies.size() < MAX_SHORT_STRATEGIES;
            }
        }

        throw new RuntimeException("action " + action + " is undefined.");
    }


}
