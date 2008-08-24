package com.jbooktrader.strategy;

import com.jbooktrader.indicator.balance.*;
import com.jbooktrader.indicator.price.*;
import com.jbooktrader.indicator.volume.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class Dicey2 extends StrategyES {

    // Technical indicators
    private final Indicator directionalVolumeInd, rsiInd, balanceEmaInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public Dicey2(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        directionalVolumeInd = new DirectionalVolume(getParam(PERIOD));
        rsiInd = new PriceRSI(getParam(PERIOD));
        balanceEmaInd = new BalanceEMA(getParam(PERIOD));
        addIndicator(directionalVolumeInd);
        addIndicator(rsiInd);
        addIndicator(balanceEmaInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(PERIOD, 50, 300, 10, 171);
        addParam(ENTRY, 35, 80, 1, 56);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double directionalVolume = directionalVolumeInd.getValue();
        double rsi = rsiInd.getValue() - 50;
        double strength = directionalVolume + rsi - balanceEmaInd.getValue();
        if (strength >= entry) {
            setPosition(-1);
        } else if (strength <= -entry) {
            setPosition(1);
        }
    }
}
