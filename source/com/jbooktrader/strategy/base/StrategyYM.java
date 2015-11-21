package com.jbooktrader.strategy.base;

import com.ib.client.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.contract.*;

/**
 * @author Eugene Kononov
 */
public abstract class StrategyYM extends Strategy {
    /*
    * MARGIN REQUIREMENTS for YM: ECBOT as of 13-July-2009
    * Source: http://www.interactivebrokers.com/en/p.php?f=margin&ib_entity=llc
    *
    * Initial Intra-day: $3,250
    * Intra-day Maintenance: $2,600
    * Initial Overnight: $6,500
    * Overnight Maintenance: $5,200
    */
    protected StrategyYM(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("YM", "ECBOT");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:00", "15:55", "America/New_York");
        int multiplier = 5;// contract multiplier
        double bidAskSpread = 1; // prevalent spread between best bid and best ask
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission, bidAskSpread);
    }

}
