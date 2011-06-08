package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;


/**
 *
 */
public class Pendulum1 extends Pendulum {
    public Pendulum1(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 360, 440, 5, 414);
        addParam(TAU, 30, 70, 5, 56);
        addParam(BALANCE_ENTRY, 29, 34, 1, 23);
        addParam(VOLUME_ENTRY, 5, 15, 1, 11);
        addParam(EXIT, 12, 18, 1, 14);
    }
}
