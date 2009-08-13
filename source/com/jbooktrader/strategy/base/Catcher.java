package com.jbooktrader.strategy.base;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public abstract class Catcher extends StrategyES {

    // Technical indicators
    private final Indicator balanceVelocityInd, trendStrengthInd;

    // Strategy parameters names
    protected static final String FAST_PERIOD = "Fast Period";
    protected static final String SLOW_PERIOD = "Slow Period";
    protected static final String TREND_PERIOD = "Trend Period";
    protected static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;

    public Catcher(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        balanceVelocityInd = new BalanceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        trendStrengthInd = new TrendStrength(getParam(TREND_PERIOD));
        addIndicator(balanceVelocityInd);
        addIndicator(trendStrengthInd);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceVelocity = balanceVelocityInd.getValue();
        double trendStrength = trendStrengthInd.getValue();
        if (trendStrength <= entry) {
            if (balanceVelocity >= entry) {
                setPosition(1);
            } else if (balanceVelocity <= -entry) {
                setPosition(-1);
            }
        }
    }
}
