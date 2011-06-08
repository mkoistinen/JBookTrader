package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;


/**
 *
 */
public class Wild2 extends Wild {
    public Wild2(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 460, 540, 5, 495);
        addParam(TAU, 20, 35, 5, 25);
        addParam(BALANCE_ENTRY, 14, 22, 1, 17);
        addParam(VOLUME_ENTRY, 7, 11, 1, 9);
        addParam(EXIT, 13, 22, 1, 18);
    }
}
