package com.jbooktrader.strategy;

import com.ib.client.Contract;
import com.jbooktrader.indicator.TruePrice;
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
public class Arbitrager extends Strategy {

    // Technical indicators
    private final Indicator truePriceInd;

    // Strategy parameters names
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final int entry, exit;


    public Arbitrager(StrategyParams optimizationParams, MarketBook marketBook) throws JBookTraderException {
        super(optimizationParams, marketBook);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);

        // Create technical indicators
        truePriceInd = new TruePrice(marketBook);

        // Specify the title and the chart number for each indicator
        // "0" = the same chart as the price chart; "1+" = separate subchart (below the price chart)
        addIndicator("Depth Balance", truePriceInd, 0);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(ENTRY, 30, 90, 1, 71);
        addParam(EXIT, 0, 50, 1, 31);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        int currentPosition = getPositionManager().getPosition();
        double truePrice = truePriceInd.getValue();
        double price = getLastMarketDepth().getMidPoint();
        double diff = truePrice - price;
        if (diff >= (entry / 100.)) {
            setPosition(1);
        } else if (diff <= (-entry / 100.)) {
            setPosition(-1);
        } else {
            boolean target = (currentPosition > 0 && diff <= (-exit / 100.));
            target = target || (currentPosition < 0 && diff >= (exit / 100.));
            if (target) {
                setPosition(0);
            }
        }
    }
}
