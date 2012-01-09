package com.jbooktrader.strategy;

import com.jbooktrader.indicator.balance.*;
import com.jbooktrader.indicator.price.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;


/**
 *
 */
public class SampleShortStrat extends StrategyES {

    // Technical indicators
    private Indicator balanceVelocityInd, priceVelocityInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String MULTIPLIER = "Multiplier";
    private static final String SCALE = "Scale";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry, scale;
    private final double exit;


    public SampleShortStrat(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        exit = -entry / 2.0;
        scale = getParam(SCALE);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 2450, 2700, 5, 2618);
        addParam(MULTIPLIER, 39, 52, 1, 45);
        addParam(SCALE, 19, 27, 1, 21);
        addParam(ENTRY, 120, 230, 1, 158);
    }

    @Override
    public void setIndicators() {
        balanceVelocityInd = addIndicator(new BalanceVelocity(1, getParam(PERIOD)));
        priceVelocityInd = addIndicator(new PriceVelocity(1, getParam(PERIOD)));

    }

    @Override
    public void onBookSnapshot() {
        double balanceVelocity = balanceVelocityInd.getValue();
        double priceVelocity = priceVelocityInd.getValue();
        double force = balanceVelocity - scale * priceVelocity;


        if (force <= -entry) {
            setPosition(-1);
        } else if (force >= exit) {
            setPosition(0);
        }
    }
}