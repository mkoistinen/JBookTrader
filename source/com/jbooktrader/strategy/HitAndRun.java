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
public class HitAndRun extends Strategy {

    // Technical indicators
    private final Indicator balanceInd, balanceVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "FastPeriod";
    private static final String SLOW_PERIOD = "SlowPeriod";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final int entry, exit;


    public HitAndRun(StrategyParams optimizationParams, MarketBook marketBook) throws JBookTraderException {
        super(optimizationParams, marketBook);

        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        int multiplier = 50;// contract multiplier

        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");

        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);
        balanceInd = new Balance(marketBook);
        balanceVelocityInd = new Velocity(balanceInd, getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        addIndicator("balance", balanceInd);
        addIndicator("balanceVelocity", balanceVelocityInd);


    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 5, 100, 25, 20);
        addParam(SLOW_PERIOD, 550, 1500, 50, 1335);
        addParam(ENTRY, 15, 50, 1, 36);
        addParam(EXIT, 7, 20, 1, 16);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceVelocity = balanceVelocityInd.getValue();
        if (balanceVelocity >= entry) {
            setPosition(1);
        } else if (balanceVelocity <= -entry) {
            setPosition(-1);
        } else {
            int currentPosition = getPositionManager().getPosition();
            if (currentPosition > 0 && balanceVelocity <= -exit) {
                setPosition(0);
            }
            if (currentPosition < 0 && balanceVelocity >= exit) {
                setPosition(0);
            }
        }
    }
}
