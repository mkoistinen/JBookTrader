package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;


/**
 *
 */
public class ShortDefender3 extends ShortDefender {

    public ShortDefender3(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 4600, 5600, 250, 5385);
        addParam(SCALE_FACTOR, 21, 32, 1, 23);
        addParam(ENTRY, 26, 30, 1, 27);
        addParam(EXIT, 25, 30, 1, 0);
    }
}
