package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class TensionSeeker extends StrategyES {

    // Technical indicators
    private final Indicator correlationInd, balanceVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "Fast Period";
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String CORRELATION_ENTRY = "Correlation Entry";
    private static final String BALANCE_VELOCITY_ENTRY = "Balance Velocity Entry";

    // Strategy parameters values
    private final int correlationEntry, balanceEntry;

    public TensionSeeker(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        correlationEntry = getParam(CORRELATION_ENTRY);
        balanceEntry = getParam(BALANCE_VELOCITY_ENTRY);
        correlationInd = new DepthPriceCorrelation();
        balanceVelocityInd = new BalanceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        addIndicator(correlationInd);
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
        //101-5893-8-19 for 10-14:00
        addParam(FAST_PERIOD, 25, 165, 1, 65);
        addParam(SLOW_PERIOD, 3000, 7500, 100, 5631);
        addParam(CORRELATION_ENTRY, 1, 20, 1, 13);
        addParam(BALANCE_VELOCITY_ENTRY, 10, 30, 1, 20);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balance = balanceVelocityInd.getValue();
        double correlation = correlationInd.getValue();
        // Negative correlation between balance and price indicates high tension,
        // positive correlation indicates no tension, i.e, "fair prices".
        if (correlation <= -correlationEntry && balance >= balanceEntry) {
            setPosition(1);
        } else if (correlation <= -correlationEntry && balance <= -balanceEntry) {
            setPosition(-1);
        }
    }
}
