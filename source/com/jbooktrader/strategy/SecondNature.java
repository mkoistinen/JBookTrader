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
    private final Indicator balanceVelocityInd, trendVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "Fast Period";
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String TREND_PERIOD = "Trend Period";
    private static final String BALANCE_ENTRY = "Balance Entry";

    // Strategy parameters values
    private final int balanceVelocityEntry;

    public SecondNature(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        balanceVelocityEntry = getParam(BALANCE_ENTRY);
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
        addParam(FAST_PERIOD, 3, 25, 1, 14);
        addParam(SLOW_PERIOD, 600, 1800, 5, 1125);
        addParam(TREND_PERIOD, 300, 600, 5, 385);
        addParam(BALANCE_ENTRY, 110, 140, 1, 126);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceVelocity = balanceVelocityInd.getValue() * 10;
        double trendVelocity = trendVelocityInd.getValue();
        if (trendVelocity < 0) {
            if (balanceVelocity >= balanceVelocityEntry) {
                setPosition(1);
            } else if (balanceVelocity <= -balanceVelocityEntry) {
                setPosition(-1);
            }
        }
    }
}
