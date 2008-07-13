package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.balance.*;
import com.jbooktrader.indicator.derivative.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

/**
 *
 */
public class Walker extends Strategy {

    // Technical indicators
    private final Indicator emaBalanceInd, emaBalanceDisplacementInd;

    // Strategy parameters names
    private static final String EMA_PERIOD = "EmaPeriod";
    private static final String DISPLACEMENT_PERIOD = "DisplacementPeriod";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public Walker(StrategyParams optimizationParams, MarketBook marketBook) throws JBookTraderException {
        super(optimizationParams, marketBook);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);

        // Create technical indicators
        emaBalanceInd = new BalanceEMA(marketBook, getParam(EMA_PERIOD));
        emaBalanceDisplacementInd = new Displacement(emaBalanceInd, getParam(DISPLACEMENT_PERIOD));

        addIndicator("emaBalance", emaBalanceInd);
        addIndicator("displacement", emaBalanceDisplacementInd);

    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(EMA_PERIOD, 1, 100, 5, 15);
        addParam(DISPLACEMENT_PERIOD, 100, 600, 5, 675);
        addParam(ENTRY, 5, 100, 5, 45);
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
