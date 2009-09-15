package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class Defender2 extends StrategyES {

    // Technical indicators
    private final Indicator balanceVelocityInd, priceVolatilityVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "Fast Period";
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String TREND_PERIOD = "Trend Period";
    private static final String ENTRY = "Entry";


    // Strategy parameters values
    private final int entry;

    public Defender2(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        balanceVelocityInd = new BalanceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        priceVolatilityVelocityInd = new PriceVolatilityVelocity(getParam(TREND_PERIOD));
        addIndicator(balanceVelocityInd);
        addIndicator(priceVolatilityVelocityInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 5, 14, 1, 10);
        addParam(SLOW_PERIOD, 2500, 4200, 100, 3080);
        addParam(TREND_PERIOD, 125, 350, 10, 205);
        addParam(ENTRY, 160, 185, 1, 175);
    }

    /**
     * Framework invokes this method when a new snapshot of the limit order book is taken
     * and the technical indicators are recalculated. This is where the strategy itself
     * (i.e., its entry and exit conditions) should be defined.
     */
    @Override
    public void onBookSnapshot() {
        double balanceVelocity = balanceVelocityInd.getValue() * 10;
        double priceVolatilityVelocity = priceVolatilityVelocityInd.getValue();

        int currentPosition = getPositionManager().getPosition();
        if (currentPosition > 0 && balanceVelocity <= -entry) {
            setPosition(0);
        }
        if (currentPosition < 0 && balanceVelocity >= entry) {
            setPosition(0);
        }

        if (priceVolatilityVelocity < 0) {
            if (balanceVelocity >= entry) {
                setPosition(1);
            } else if (balanceVelocity <= -entry) {
                setPosition(-1);
            }
        }

    }
}
