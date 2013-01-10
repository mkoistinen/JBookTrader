package com.jbooktrader.strategy.base;

import com.ib.client.Contract;
import com.jbooktrader.platform.commission.Commission;
import com.jbooktrader.platform.commission.CommissionFactory;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ContractFactory;

/**
 *
 */
public abstract class StrategyCL extends Strategy {
    /*
    * MARGIN REQUIREMENTS for CL: GLOBEX as of 8-Jan-2013
    * Source: http://www.interactivebrokers.com/en/index.php?f=margin&p=fut
    *
    * Initial Intra-day: $3,188
    * Intra-day Maintenance: $2,550
    * Initial Overnight: $6,375
    * Overnight Maintenance: $5,100
    */
    protected StrategyCL(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = getNewContract();
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:35", "14:55", "America/New_York");
        int multiplier = 1000;// contract 1000 barrels
        double bidAskSpread = 0.01; // prevalent spread between best bid and best ask
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission, bidAskSpread);
    }

    @Override
    public Contract getNewContract() {
        return ContractFactory.makeNYMEXFutureContract("CL", "NYMEX");
    }
}
