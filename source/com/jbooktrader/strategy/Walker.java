package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.indicator.derivative.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class Walker extends StrategyES {

    // Technical indicators
    private final Indicator emaBalanceDisplacementInd;

    // Strategy parameters names
    private static final String EMA_PERIOD = "EmaPeriod";
    private static final String DISPLACEMENT_PERIOD = "DisplacementPeriod";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public Walker(StrategyParams optimizationParams) {
        super(optimizationParams);

        entry = getParam(ENTRY);
        // Create technical indicators
        Indicator emaBalanceInd = new DepthBalanceEMA(getParam(EMA_PERIOD));
        emaBalanceDisplacementInd = new Displacement(emaBalanceInd, getParam(DISPLACEMENT_PERIOD));

        addIndicator(emaBalanceInd);
        addIndicator(emaBalanceDisplacementInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(EMA_PERIOD, 5, 100, 1, 12);
        addParam(DISPLACEMENT_PERIOD, 200, 600, 1, 460);
        addParam(ENTRY, 25, 80, 1, 50);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double displacement = emaBalanceDisplacementInd.getValue();
        if (displacement >= entry) {
            setPosition(1);
        } else if (displacement <= -entry) {
            setPosition(-1);
        }
    }
}
