package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.balance.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

/**
 *
 */
public class Scalper1 extends Strategy {

    // Technical indicators
    private final Indicator balanceHighInd, balanceLowInd;

    // Strategy parameters names
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final int entry, exit;


    public Scalper1(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        int multiplier = 50;// contract multiplier

        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:35", "15:55", "America/New_York");

        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);
        balanceHighInd = new BalanceHighEMA(1);
        balanceLowInd = new BalanceLowEMA(1);
        addIndicator(balanceLowInd);
        addIndicator(balanceHighInd);

    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(ENTRY, 30, 80, 1, 58);
        addParam(EXIT, 0, 50, 1, 30);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceHigh = balanceHighInd.getValue();
        double balanceLow = balanceLowInd.getValue();
        if (balanceHigh >= entry) {
            setPosition(1);
        } else if (balanceLow <= -entry) {
            setPosition(-1);
        } else {
            int currentPosition = getPositionManager().getPosition();
            if (currentPosition > 0 && balanceHigh <= -exit) {
                setPosition(0);
            }
            if (currentPosition < 0 && balanceLow >= exit) {
                setPosition(0);
            }
        }
    }
}
