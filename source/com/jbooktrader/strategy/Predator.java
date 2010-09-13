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
    private final Indicator balanceVelocityInd, priceTrendVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "Fast Period";
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String MAGNIFIER = "Magnifier";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry, magnifier;

    public Predator(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        magnifier = getParam(MAGNIFIER);
        balanceVelocityInd = new BalanceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        priceTrendVelocityInd = new PriceTrendVelocity(getParam(SLOW_PERIOD));
        addIndicator(balanceVelocityInd);
        addIndicator(priceTrendVelocityInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 5, 250, 1, 202);
        addParam(SLOW_PERIOD, 6000, 13000, 100, 11554);
        addParam(MAGNIFIER, 3, 13, 1, 10);
        addParam(ENTRY, 17, 25, 1, 19);

    }

    /**
     * Framework invokes this method when a new snapshot of the limit order book is taken
     * and the technical indicators are recalculated. This is where the strategy itself
     * (i.e., its entry and exit conditions) should be defined.
     */
    @Override
    public void onBookSnapshot() {
        double balanceVelocity = balanceVelocityInd.getValue();
        double priceTrendVelocity = priceTrendVelocityInd.getValue();

        double power = balanceVelocity - magnifier * priceTrendVelocity;
        if (power >= entry) {
            setPosition(1);
        } else if (power <= -entry) {
            setPosition(-1);
        }
    }
}
