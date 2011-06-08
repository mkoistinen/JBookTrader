package com.jbooktrader.strategy.common;

import com.jbooktrader.indicator.combo.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;


/**
 *
 */
public abstract class ShortDefender extends StrategyES {

    // Technical indicators
    private Indicator tensionInd;

    // Strategy parameters names
    protected static final String PERIOD = "Period";
    protected static final String SCALE_FACTOR = "Scale Factor";
    protected static final String ENTRY = "Entry";
    protected static final String EXIT = "Exit";


    // Strategy parameters values
    private final int entry, exit;

    protected ShortDefender(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        entry = getParam(ENTRY);
        exit = getParam(EXIT);
    }


    @Override
    public void setIndicators() {
        tensionInd = addIndicator(new Tension(getParam(PERIOD), getParam(SCALE_FACTOR)));
    }

    @Override
    public void onBookSnapshot() {
        double tension = tensionInd.getValue();
        if (tension <= -entry) {
            setPosition(-1);
        } else if (tension >= exit) {
            setPosition(0);
        }
    }
}
