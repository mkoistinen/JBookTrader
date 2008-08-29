package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.indicator.index.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class Vixer extends StrategyES {

    // Technical indicators
    private final Indicator balanceEmaInd, vixIndexEmaInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String BALANCE_ENTRY = "Depth Balance Entry";
    private static final String VIX_ENTRY = "Vix Entry";


    // Strategy parameters values
    private final int balanceEntry, vixEntry;


    public Vixer(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        balanceEntry = getParam(BALANCE_ENTRY);
        vixEntry = getParam(VIX_ENTRY);

        // Create technical indicators
        balanceEmaInd = new DepthBalanceEMA(getParam(PERIOD));
        vixIndexEmaInd = new VixIndexEMA(getParam(PERIOD));

        addIndicator(balanceEmaInd);
        addIndicator(vixIndexEmaInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(PERIOD, 1, 50, 1, 1);
        addParam(BALANCE_ENTRY, 0, 50, 1, 25);
        addParam(VIX_ENTRY, 1, 50, 1, 10);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceEma = balanceEmaInd.getValue();
        double vixIndex = vixIndexEmaInd.getValue();
        if (balanceEma >= balanceEntry && vixIndex >= vixEntry) {
            setPosition(1);
        } else if (balanceEma <= -balanceEntry && vixIndex >= vixEntry) {
            setPosition(-1);
        }
    }
}
