package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class Hybrid2 extends StrategyES {

    // Technical indicators
    private final Indicator balanceEMAInd, balanceVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "FastPeriod";
    private static final String SLOW_PERIOD = "SlowPeriod";
    private static final String ENTRY = "Entry";
    private final int entry;


    public Hybrid2(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        balanceEMAInd = new DepthBalanceEMA(getParam(FAST_PERIOD));
        balanceVelocityInd = new BalanceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        addIndicator(balanceEMAInd);
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
        addParam(FAST_PERIOD, 1, 2000, 1, 1042);
        addParam(SLOW_PERIOD, 3000, 12000, 1, 6340);
        addParam(ENTRY, 1, 50, 1, 22);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balance = balanceEMAInd.getValue() + balanceVelocityInd.getValue();
        if (balance >= entry) {
            setPosition(1);
        } else if (balance <= -entry) {
            setPosition(-1);
        }
    }
}
