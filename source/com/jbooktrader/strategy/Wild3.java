package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;


/**
 *
 */
public class Wild3 extends Wild {
    public Wild3(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 450, 540, 5, 491);
        addParam(TAU, 0, 50, 5, 30);
        addParam(BALANCE_ENTRY, 15, 19, 1, 18);
        addParam(VOLUME_ENTRY, 7, 11, 1, 9);
        addParam(EXIT, 16, 20, 1, 17);
    }
}
