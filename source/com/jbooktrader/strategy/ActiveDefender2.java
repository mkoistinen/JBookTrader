package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class ActiveDefender2 extends StrategyES {

    // Technical indicators
    private Indicator tensionInd;

    // Strategy parameters names
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String SCALE_FACTOR = "Scale Factor";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";


    // Strategy parameters values
    private final int entry, exit;

    public ActiveDefender2(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        entry = getParam(ENTRY);
        exit = getParam(EXIT);
    }


    @Override
    public void setIndicators() {
        tensionInd = addIndicator(new Tension(1, getParam(SLOW_PERIOD), getParam(SCALE_FACTOR)));
    }


    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(SLOW_PERIOD, 1200, 2700, 100, 952);
        addParam(SCALE_FACTOR, 10, 35, 100, 37);
        addParam(ENTRY, 26, 33, 1, 29);
        addParam(EXIT, 1, 14, 1, 15);
    }

    /**
     * Framework invokes this method when a new snapshot of the limit order book is taken
     * and the technical indicators are recalculated. This is where the strategy itself
     * (i.e., its entry and exit conditions) should be defined.
     */
    @Override
    public void onBookSnapshot() {
        double tension = tensionInd.getValue();
        if (tension >= entry) {
            setPosition(1);
        } else if (tension <= -entry) {
            setPosition(-1);
        } else {
            int currentPosition = getPositionManager().getCurrentPosition();
            if (tension >= exit && currentPosition < 0) {
                setPosition(0);
            }
            if (tension <= -exit && currentPosition > 0) {
                setPosition(0);
            }
        }
    }
}
