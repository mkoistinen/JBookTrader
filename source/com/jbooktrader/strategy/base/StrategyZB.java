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
public abstract class StrategyZB extends Strategy {
    /*
    * MARGIN REQUIREMENTS for ZB: GLOBEX as of 13-July-2009
    * Source: http://www.interactivebrokers.com/en/p.php?f=margin&ib_entity=llc
    *
    * Initial Intra-day: $1,856
    * Intra-day Maintenance: $1,375
    * Initial Overnight: $3,713
    * Overnight Maintenance: $2,750
    */
    protected StrategyZB(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ZB", "ECBOT");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:00", "15:30", "America/New_York");
        int multiplier = 1000;// contract multiplier
        double bidAskSpread = 0.03125; // prevalent spread between best bid and best ask
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission, bidAskSpread);
    }

}
