package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class VolumeSeeker extends StrategyES {

    // Technical indicators
    private Indicator balanceVelocityInd, priceVelocityInd, volumeVelocityInd;

    // Strategy parameters names
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String DIVIDER = "Divider";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";


    // Strategy parameters values
    private final int entry, exit, divider;

    public VolumeSeeker(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        entry = getParam(ENTRY);
        exit = getParam(EXIT);
        divider = getParam(DIVIDER);
    }

    @Override
    public void setIndicators() {
        balanceVelocityInd = addIndicator(new BalanceVelocity(1, getParam(SLOW_PERIOD)));
        priceVelocityInd = addIndicator(new PriceTrendVelocitySMA(getParam(SLOW_PERIOD)));
        volumeVelocityInd = addIndicator(new VolumeVelocitySMA(1, getParam(SLOW_PERIOD)));
    }


    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(SLOW_PERIOD, 3200, 6700, 50, 5326);
        addParam(DIVIDER, 10, 35, 1, 20);
        addParam(ENTRY, 32, 42, 1, 34);
        addParam(EXIT, 11, 18, 1, 14);
    }

    /**
     * Framework invokes this method when a new snapshot of the limit order book is taken
     * and the technical indicators are recalculated. This is where the strategy itself
     * (i.e., its entry and exit conditions) should be defined.
     */
    @Override
    public void onBookSnapshot() {
        double priceVelocity = priceVelocityInd.getValue();
        double balanceVelocity = balanceVelocityInd.getValue();
        double volumeVelocity = volumeVelocityInd.getValue();
        volumeVelocity = balanceVelocity > 0 ? volumeVelocity : -volumeVelocity;
        double superTension = balanceVelocity + (volumeVelocity / divider) - priceVelocity;

        if (superTension >= entry) {
            setPosition(1);
        } else if (superTension <= -entry) {
            setPosition(-1);
        } else {
            int currentPosition = getPositionManager().getCurrentPosition();
            if (superTension >= exit && currentPosition < 0) {
                setPosition(0);
            }
            if (superTension <= -exit && currentPosition > 0) {
                setPosition(0);
            }
        }

    }
}
