package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class Equalizer extends StrategyES {

    // Technical indicators
    private final Indicator balanceVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "FastPeriod";
    private static final String SLOW_PERIOD = "SlowPeriod";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public Equalizer(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        balanceVelocityInd = new BalanceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        addIndicator(balanceVelocityInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 3, 50, 1, 7);
        addParam(SLOW_PERIOD, 1000, 5000, 100, 3175);
        addParam(ENTRY, 15, 21, 1, 18);
    }

    /**
     * Framework invokes this method when a new snapshot of the the order book is taken
     * and the technical indicators are recalculated. This is where the strategy itself
     * (i.e., its entry and exit criteria) should be defined.
     */
    @Override
    public void onBookSnapshot() {
        double balanceVelocity = balanceVelocityInd.getValue();
        if (balanceVelocity >= entry) {
            setPosition(1);
        } else if (balanceVelocity <= -entry) {
            setPosition(-1);
        }
    }
}
