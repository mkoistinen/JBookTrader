package com.jbooktrader.platform.strategy;

import java.util.LinkedList;

import com.jbooktrader.platform.marketbook.MarketBook;
import com.jbooktrader.platform.marketbook.MarketSnapshot;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.model.ModelListener;
import com.jbooktrader.platform.performance.PerformanceManager;
import com.jbooktrader.platform.position.PositionManager;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.util.ClassFinder;

public class CommandLineStrategyRunner {

    private class CommandLineModelListener implements ModelListener {

        public void modelChanged(Event event, Object value) {
            switch (event) {
            case ModeChanged:
                String subTitle = "";
                switch (Dispatcher.getMode()) {
                case Trade:
                    subTitle = "Trading";
                    break;
                case BackTest:
                    subTitle = "Back Testing";
                    break;
                case ForwardTest:
                    subTitle = "Forward Testing";
                    break;
                case Optimization:
                    subTitle = "Optimizing";
                    break;
                default:
                    subTitle = "ERROR: unknown running mode";
                break;
                }
                System.err.println(JBookTrader.APP_NAME + " " + JBookTrader.VERSION + " - [" + subTitle + "]");
                break;
            case Error:
                System.err.println("ERROR: "+(String) value);
                break;
            case StrategyUpdate:
                StringBuilder msg = new StringBuilder();
                Strategy strategy = (Strategy) value;
                msg.append(strategy.getName());
                msg.append(": ");
                MarketBook marketBook = strategy.getMarketBook();
                if (marketBook.size() > 0) {
                    MarketSnapshot lastMarketSnapshot = marketBook.getLastMarketSnapshot();
                    msg.append("bid:");
                    msg.append(lastMarketSnapshot.getBestBid());
                    msg.append(", ask:");
                    msg.append(lastMarketSnapshot.getBestAsk());
                    msg.append(", volume:");
                    msg.append(marketBook.getCumulativeVolume());
                }
                PositionManager positionManager = strategy.getPositionManager();
                PerformanceManager performanceManager = strategy.getPerformanceManager();
                msg.append(", position:");
                msg.append(positionManager.getPosition());
                msg.append(", trades:");
                msg.append(performanceManager.getTrades());
                msg.append(", maxdd:");
                msg.append(performanceManager.getMaxDrawdown());
                msg.append(", netprofit:");
                msg.append(performanceManager.getNetProfit());
                System.err.println(msg.toString());
                break;
            case StrategiesStart:
                System.err.println("Strategy started");
                break;
            case StrategiesEnd:
                System.err.println("Strategy ended");
                break;
            default:
                System.err.println("ERROR: unknown dispatcher event");
                break;
            }
        }
    }

    public CommandLineStrategyRunner(Dispatcher.Mode mode, String[] strategyNames) throws JBookTraderException {

        LinkedList<Strategy> strategies = new LinkedList<Strategy>(); 
        for(int i=0; i<strategyNames.length; i++) {
            strategies.add(ClassFinder.getInstance(strategyNames[i]));
        }

        Dispatcher.addListener(new CommandLineModelListener());
        Dispatcher.setMode(mode);

        for(Strategy strategy: strategies) {
            Dispatcher.getTrader().getAssistant().addStrategy(strategy);
        }
    }
}
