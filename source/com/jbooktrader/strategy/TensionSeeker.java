package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class TensionSeeker extends StrategyES {

    // Technical indicators
    private final DepthPriceCorrelation correlationInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final int entry, exit;

    public TensionSeeker(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);
        correlationInd = new DepthPriceCorrelation(getParam(PERIOD));
        addIndicator(correlationInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(PERIOD, 1800, 3000, 10, 2177);
        addParam(ENTRY, 55, 95, 1, 82);
        addParam(EXIT, 0, 20, 1, 7);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double correlationMagnitude = Math.abs(correlationInd.getValue());
        double priceSlope = correlationInd.getPriceSlope();
        int currentPosition = getPositionManager().getPosition();

        if (currentPosition == 0) {
            if (correlationMagnitude >= entry && priceSlope > 0) { //resistance
                setPosition(-1);
            }
            if (correlationMagnitude >= entry && priceSlope < 0) { //support
                setPosition(1);
            }
        } else {
            if (correlationMagnitude <= exit) {
                setPosition(0);
            }
        }
    }
}
