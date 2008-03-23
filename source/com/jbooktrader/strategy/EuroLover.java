package com.jbooktrader.strategy;

import com.ib.client.Contract;
import com.jbooktrader.indicator.DepthBalance;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.marketdepth.MarketBook;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ContractFactory;

/**
 *
 */
public class EuroLover extends Strategy {

    // Technical indicators
    private final Indicator depthBalanceInd;

    // Strategy parameters names
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final double entry, exit;


    public EuroLover(StrategyParams optimizationParams, MarketBook marketBook) throws JBookTraderException {
        super(optimizationParams, marketBook);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeCashContract("EUR", "USD");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");
        int multiplier = 1;// contract multiplier
        Commission commission = CommissionFactory.getForexCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);

        // Create technical indicators
        depthBalanceInd = new DepthBalance(marketBook);

        // Specify the title and the chart number for each indicator
        // "0" = the same chart as the price chart; "1+" = separate subchart (below the price chart)
        addIndicator("Depth Balance", depthBalanceInd, 1);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(ENTRY, 20, 70, 1, 23);
        addParam(EXIT, 0, 60, 1, 18);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        int currentPosition = getPositionManager().getPosition();
        double depthBalance = depthBalanceInd.getValue();
        if (depthBalance >= entry) {
            setPosition(125000);
        } else if (depthBalance <= -entry) {
            setPosition(-125000);
        } else {
            boolean target = (currentPosition > 0 && depthBalance <= -exit);
            target = target || (currentPosition < 0 && depthBalance >= exit);
            if (target) {
                setPosition(0);
            }
        }
    }
}
