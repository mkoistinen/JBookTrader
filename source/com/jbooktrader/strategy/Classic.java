package com.jbooktrader.strategy;

import com.ib.client.Contract;
import com.jbooktrader.indicator.DepthBalance;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ContractFactory;
import com.jbooktrader.platform.marketdepth.MarketBook;

/**
 *
 */
public class Classic extends Strategy {

    // Technical indicators
    private final Indicator depthBalanceInd;

    // Strategy parameters names
    private static final String ENTRY = "Entry";
    private static final String STOP_LOSS = "Stop Loss";
    private static final String PROFIT_TARGET = "Profit Target";

    // Strategy parameters values
    private final double entry, stopLoss, profitTarget;


    public Classic(StrategyParams params, MarketBook marketBook) throws JBookTraderException {
        super(marketBook);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        // Initialize strategy parameter values. If the strategy is running in the optimization
        // mode, the parameter values will be taken from the "params" object. Otherwise, the
        // "params" object will be empty and the parameter values will be initialized to the
        // specified default values.
        entry = params.get(ENTRY, 43);
        stopLoss = params.get(STOP_LOSS, 10);
        profitTarget = params.get(PROFIT_TARGET, 14);

        // Create technical indicators
        depthBalanceInd = new DepthBalance(marketBook);

        // Specify the title and the chart number for each indicator
        // "0" = the same chart as the price chart; "1+" = separate subchart (below the price chart)
        addIndicator("Depth Balance", depthBalanceInd, 1);
    }

    /**
     * Returns min/max/step values for each strategy parameter. This method is
     * invoked by the strategy optimizer to obtain the strategy parameter ranges.
     */
    @Override
    public StrategyParams initParams() {
        StrategyParams params = new StrategyParams();
        params.add(ENTRY, 20, 50, 1);
        params.add(STOP_LOSS, 2, 20, 1);
        params.add(PROFIT_TARGET, 1, 20, 1);
        return params;
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
            setPosition(1);
        } else if (depthBalance <= -entry) {
            setPosition(-1);
        } else {
            double lastEntry = getPositionManager().getAvgFillPrice();
            double currentPrice = getLastMarketDepth().getMidPoint();
            double loss = (currentPosition > 0) ? (lastEntry - currentPrice) : (currentPrice - lastEntry);
            double gain = -loss;
            if (loss >= stopLoss || gain >= profitTarget) {
                setPosition(0);
            }
        }
    }
}
