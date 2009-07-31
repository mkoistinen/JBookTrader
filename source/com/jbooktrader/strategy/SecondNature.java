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

    // Strategy parameters values
    private final int balanceEntry;

    public SecondNature(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        balanceEntry = getParam(BALANCE_ENTRY);
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
        addParam(FAST_PERIOD, 80, 230, 1, 105);
        addParam(SLOW_PERIOD, 5500, 7500, 100, 6310);
        addParam(TREND_PERIOD, 500, 1500, 100, 724);
        addParam(BALANCE_ENTRY, 12, 22, 1, 19);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceVelocity = balanceVelocityInd.getValue();
        double trendVelocity = trendVelocityInd.getValue();
        if (trendVelocity < 0) {
            if (balanceVelocity >= balanceEntry) {
                setPosition(1);
            } else if (balanceVelocity <= -balanceEntry) {
                setPosition(-1);
            }
        }
    }
}
