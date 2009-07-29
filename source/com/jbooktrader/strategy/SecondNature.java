package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class SecondNature extends StrategyES {

    // Technical indicators
    private final Indicator balanceVelocityInd, trendVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "Fast Period";
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String TREND_PERIOD = "Trend Period";
    private static final String BALANCE_ENTRY = "Balance Entry";
    private static final String TREND_ENTRY = "Trend Entry";

    // Strategy parameters values
    private final int balanceEntry, trendEntry;

    public SecondNature(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        balanceEntry = getParam(BALANCE_ENTRY);
        trendEntry = getParam(TREND_ENTRY);
        balanceVelocityInd = new BalanceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        trendVelocityInd = new TrendStrengthVelocity(getParam(TREND_PERIOD));
        addIndicator(balanceVelocityInd);
        addIndicator(trendVelocityInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 120, 200, 1, 243);
        addParam(SLOW_PERIOD, 4000, 9000, 500, 5291);
        addParam(TREND_PERIOD, 100, 2000, 100, 1102);
        addParam(BALANCE_ENTRY, 12, 20, 1, 14);
        addParam(TREND_ENTRY, -2, 1, 1, -1);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceVelocity = balanceVelocityInd.getValue();
        double trendVelocity = trendVelocityInd.getValue();
        if (trendVelocity < trendEntry) {
            if (balanceVelocity >= balanceEntry) {
                setPosition(1);
            } else if (balanceVelocity <= -balanceEntry) {
                setPosition(-1);
            }
        }
    }
}
