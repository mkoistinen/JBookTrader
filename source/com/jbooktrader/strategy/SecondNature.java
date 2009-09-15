package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class SecondNature extends StrategyES {

    // Technical indicators
    private final Indicator balanceVelocityInd, trendStrengthVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "Fast Period";
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String TREND_PERIOD = "Trend Period";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;

    public SecondNature(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        balanceVelocityInd = new BalanceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        trendStrengthVelocityInd = new TrendStrengthVelocity(getParam(TREND_PERIOD));
        addIndicator(balanceVelocityInd);
        addIndicator(trendStrengthVelocityInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 8, 17, 1, 11);
        addParam(SLOW_PERIOD, 2500, 4000, 100, 2076);
        addParam(TREND_PERIOD, 250, 600, 10, 536);
        addParam(ENTRY, 158, 170, 1, 164);
    }

    /**
     * Framework invokes this method when a new snapshot of the limit order book is taken
     * and the technical indicators are recalculated. This is where the strategy itself
     * (i.e., its entry and exit conditions) should be defined.
     */
    @Override
    public void onBookSnapshot() {
        double balanceVelocity = balanceVelocityInd.getValue() * 10;
        double trendStrengthVelocity = trendStrengthVelocityInd.getValue();
        if (trendStrengthVelocity < 0) {
            if (balanceVelocity >= entry) {
                setPosition(1);
            } else if (balanceVelocity <= -entry) {
                setPosition(-1);
            }
        }
    }
}
