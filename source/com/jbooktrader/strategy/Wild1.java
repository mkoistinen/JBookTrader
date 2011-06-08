package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;


/**
 *
 */
public class Wild1 extends Wild {
    public Wild1(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 480, 512, 5, 498);
        addParam(TAU, 25, 55, 5, 24);
        addParam(BALANCE_ENTRY, 17, 22, 1, 17);
        addParam(VOLUME_ENTRY, 8, 11, 1, 9);
        addParam(EXIT, 14, 19, 1, 18);
    }
}
