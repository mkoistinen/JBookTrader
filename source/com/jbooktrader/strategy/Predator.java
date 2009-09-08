package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class Predator extends StrategyES {

    // Technical indicators
    private final Indicator balanceVelocityInd, trendVelocityInd;

    // Strategy parameters names
    protected static final String FAST_PERIOD = "Fast Period";
    protected static final String SLOW_PERIOD = "Slow Period";
    protected static final String TREND_PERIOD = "Trend Period";
    protected static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;

    public Predator(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
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
        addParam(FAST_PERIOD, 1, 25, 1, 7);
        addParam(SLOW_PERIOD, 2000, 4000, 100, 2750);
        addParam(TREND_PERIOD, 400, 800, 10, 467);
        addParam(ENTRY, 16, 22, 1, 18);
    }

    /**
     * Framework invokes this method when a new snapshot of the limit order book is taken
     * and the technical indicators are recalculated. This is where the strategy itself
     * (i.e., its entry and exit conditions) should be defined.
     */
    @Override
    public void onBookSnapshot() {
        double balanceVelocity = balanceVelocityInd.getValue();
        double trendVelocity = trendVelocityInd.getValue();

        double power = balanceVelocity > 0 ? (balanceVelocity - trendVelocity) : (balanceVelocity + trendVelocity);
        if (power >= entry) {
            setPosition(1);
        } else if (power <= -entry) {
            setPosition(-1);
        }
    }
}
