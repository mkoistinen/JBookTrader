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
public class Accelerator extends Strategy {

    // Technical indicators
    private final Indicator balanceInd, balanceAccelerationInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "FastPeriod";
    private static final String SLOW_PERIOD = "SlowPeriod";
    private static final String SMOOTHING_PERIOD = "SmoothingPeriod";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public Accelerator(StrategyParams optimizationParams, MarketBook marketBook) throws JBookTraderException {
        super(optimizationParams, marketBook);

        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        int multiplier = 50;// contract multiplier

        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");

        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);

        balanceInd = new Balance(marketBook);
        balanceAccelerationInd = new Acceleration(balanceInd, getParam(FAST_PERIOD), getParam(SLOW_PERIOD), getParam(SMOOTHING_PERIOD));
        addIndicator("balance", balanceInd);
        addIndicator("acceleration", balanceAccelerationInd);


    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 50, 300, 5, 110);
        addParam(SLOW_PERIOD, 500, 1000, 100, 980);
        addParam(SMOOTHING_PERIOD, 500, 700, 100, 685);
        addParam(ENTRY, 10, 30, 1, 18);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceAcceleration = balanceAccelerationInd.getValue();
        if (balanceAcceleration >= entry) {
            setPosition(1);
        } else if (balanceAcceleration <= -entry) {
            setPosition(-1);
        }
    }
}
