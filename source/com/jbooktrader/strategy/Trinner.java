package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.indicator.index.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class Trinner extends StrategyES {

    // Technical indicators
    private final Indicator balanceEmaInd, trinIndexEmaInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String BALANCE_ENTRY = "Depth Balance Entry";
    private static final String TRIN_ENTRY = "Trin Entry";


    // Strategy parameters values
    private final int balanceEntry, trinEntry;


    public Trinner(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        balanceEntry = getParam(BALANCE_ENTRY);
        trinEntry = getParam(TRIN_ENTRY);

        // Create technical indicators
        balanceEmaInd = new DepthBalanceEMA(getParam(PERIOD));
        trinIndexEmaInd = new TrinIndexEMA(getParam(PERIOD));

        addIndicator(balanceEmaInd);
        addIndicator(trinIndexEmaInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(PERIOD, 1, 100, 1, 13);
        addParam(BALANCE_ENTRY, 0, 50, 1, 10);
        addParam(TRIN_ENTRY, 0, 10, 1, 0);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceEma = balanceEmaInd.getValue();
        if (trinIndexEmaInd.getValue() != 0) {
            double trinIndex = (trinIndexEmaInd.getValue() - 1) * 10;
            if (balanceEma >= balanceEntry && trinIndex >= trinEntry) {
                setPosition(1);
            } else if (balanceEma <= -balanceEntry && trinIndex <= -trinEntry) {
                setPosition(-1);
            }
        }
    }
}
