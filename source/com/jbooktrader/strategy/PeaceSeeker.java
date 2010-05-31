package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class PeaceSeeker extends StrategyES {

    // Technical indicators
    private final Indicator balanceVelocityInd, volatilityVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "Fast Period";
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String TREND_PERIOD = "Trend Period";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;

    public PeaceSeeker(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        balanceVelocityInd = new BalanceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        volatilityVelocityInd = new PriceVolatilityVelocity(getParam(TREND_PERIOD));
        addIndicator(balanceVelocityInd);
        addIndicator(volatilityVelocityInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 5, 100, 1, 25);
        addParam(SLOW_PERIOD, 1000, 5000, 100, 3360);
        addParam(TREND_PERIOD, 120, 600, 10, 266);
        addParam(ENTRY, 1, 20, 1, 15);
    }

    /**
     * Framework invokes this method when a new snapshot of the limit order book is taken
     * and the technical indicators are recalculated. This is where the strategy itself
     * (i.e., its entry and exit conditions) should be defined.
     */
    @Override
    public void onBookSnapshot() {
        double balanceVelocity = balanceVelocityInd.getValue();
        double volatilityVelocity = volatilityVelocityInd.getValue();
        if (volatilityVelocity <= 0) {
            if (balanceVelocity >= entry) {
                setPosition(1);
            } else if (balanceVelocity <= -entry) {
                setPosition(-1);
            }
        }
    }
}
