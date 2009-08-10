package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class Predator extends StrategyES {

    // Technical indicators
    private final Indicator balanceVelocityInd, trendVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "Fast Period";
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String TREND_PERIOD = "Trend Period";
    private static final String ENTRY = "Entry";

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
        addParam(FAST_PERIOD, 2, 25, 1, 6);
        addParam(SLOW_PERIOD, 1000, 3000, 100, 2630);
        addParam(TREND_PERIOD, 400, 600, 100, 510);
        addParam(ENTRY, 12, 24, 1, 19);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
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
