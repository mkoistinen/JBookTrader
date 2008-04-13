package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.*;
import com.jbooktrader.platform.bar.*;
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
public class Scalper extends Strategy {

    // Technical indicators
    private final Indicator depthBalanceInd;

    // Strategy parameters names
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public Scalper(StrategyParams optimizationParams, MarketBook marketBook, PriceHistory priceHistory) throws JBookTraderException {
        super(optimizationParams, marketBook, priceHistory);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);

        // Create technical indicators
        depthBalanceInd = new DepthBalance(marketBook);
        addIndicator("Depth Balance", depthBalanceInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(ENTRY, 20, 100, 1, 38);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double depthBalance = depthBalanceInd.getValue();
        if (depthBalance >= entry) {
            setPosition(1);
        } else if (depthBalance <= -entry) {
            setPosition(-1);
        }
    }
}
