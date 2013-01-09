package com.jbooktrader.strategy;

import com.jbooktrader.indicator.balance.BalanceVelocity;
import com.jbooktrader.indicator.price.PriceKalman;
import com.jbooktrader.indicator.price.PriceVelocity;
import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.strategy.base.StrategyCL;
import com.jbooktrader.strategy.base.StrategyES;


/**
 *
 */
public class SampleCL extends StrategyCL {

    // Technical indicators
    private Indicator balanceVelocityInd, priceVelocityInd, kalmanInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String SCALE = "Scale";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";
    private static final String NOISE = "Noise";

    // Strategy parameters values
    private final int entry, exit, scale, noise;


    public SampleCL(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);
        scale = getParam(SCALE);
        noise = getParam(NOISE);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 2200, 3600, 5, 3515);
        addParam(SCALE, 5, 25, 1, 25);
        addParam(ENTRY, 55, 120, 1, 95);
        addParam(EXIT, -50, 0, 1, 0);
        addParam(NOISE, 1, 50 , 1 , 2);
    }

    @Override
    public void setIndicators() {
        balanceVelocityInd = addIndicator(new BalanceVelocity(1, getParam(PERIOD)));
        priceVelocityInd = addIndicator(new PriceVelocity(1, getParam(PERIOD)));
        kalmanInd = addIndicator(new PriceKalman(getParam(NOISE)));

    }

    @Override
    public void onBookSnapshot() {
        double balanceVelocity = balanceVelocityInd.getValue();
        double priceVelocity = priceVelocityInd.getValue();

        double force = balanceVelocity - scale * priceVelocity;
        if (force >= entry && balanceVelocity > 0 && priceVelocity < 0) {
            setPosition(1);
        } else if (force <= -exit) {
            setPosition(0);
        }
    }
}