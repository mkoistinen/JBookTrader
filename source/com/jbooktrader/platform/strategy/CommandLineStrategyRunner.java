package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.*;

import java.util.*;

public class CommandLineStrategyRunner {

    private class CommandLineModelListener implements ModelListener {

        private HashMap<String, Integer> eventCountByStrategy = new HashMap<String,Integer>();

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
                    System.err.println("ERROR: " + (String) value);
                    break;
                case StrategyUpdate:
                    Strategy strategy = (Strategy) value;
                    String strategyName = strategy.getName();
                    // Only show 1 out of 10 updates to reduce verbosity
                    Integer eventCount = eventCountByStrategy.get(strategyName);
                    if(eventCount==null) {
                        eventCountByStrategy.put(strategyName, new Integer(0));
                    }
                    else {
                        eventCount++;
                        eventCountByStrategy.put(strategyName, eventCount);
                        if(eventCount%10!=0) {
                            break;
                        }
                    }

                    StringBuilder msg = new StringBuilder();
                    msg.append(strategyName);
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
        for (int i = 0; i < strategyNames.length; i++) {
            strategies.add(ClassFinder.getInstance(strategyNames[i]));
        }

        Dispatcher.addListener(new CommandLineModelListener());
        Dispatcher.setMode(mode);

        for (Strategy strategy : strategies) {
            Dispatcher.getTrader().getAssistant().addStrategy(strategy);
        }
    }
}
