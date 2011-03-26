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
        addParam(PERIOD, 5000, 6000, 100, 5390);
        addParam(SCALE_FACTOR, 18, 28, 1, 23);
        addParam(ENTRY, 22, 32, 1, 27);
        addParam(EXIT, -5, 5, 1, 1);
    }
}
