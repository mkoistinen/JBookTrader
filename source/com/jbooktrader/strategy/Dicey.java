package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.indicator.price.*;
import com.jbooktrader.indicator.volume.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class Dicey extends StrategyES {

    // Technical indicators
    private final Indicator directionalVolumeInd;
    private final Indicator rsiInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public Dicey(StrategyParams optimizationParams) {
        super(optimizationParams);

        entry = getParam(ENTRY);
        directionalVolumeInd = new DirectionalVolume(getParam(PERIOD));
        rsiInd = new PriceRSI(getParam(PERIOD));
        Indicator balanceEmaInd = new DepthBalanceEMA(getParam(PERIOD));
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
        addParam(PERIOD, 50, 300, 10, 195);
        addParam(ENTRY, 35, 80, 1, 74);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double directionalVolume = directionalVolumeInd.getValue();
        double rsi = rsiInd.getValue() - 50;
        double strength = directionalVolume + rsi;
        if (strength >= entry) {
            setPosition(-1);
        } else if (strength <= -entry) {
            setPosition(1);
        }
    }
}
