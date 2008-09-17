package com.jbooktrader.platform.strategy;

import java.util.LinkedList;

import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.model.ModelListener;
import com.jbooktrader.platform.util.ClassFinder;

public class CommandLineStrategyRunner {

    private class CommandLineModelListener implements ModelListener {

        public void modelChanged(Event event, Object value) {

            if(event == Event.StrategiesStart) {
                //TODO
            }
            else if(event == Event.StrategiesEnd) {
                //TODO
            }
            else if(event == Event.ModeChanged) {
                //TODO
            }
            else if(event == Event.StrategyUpdate) {
                //TODO
            }
            else if(event == Event.Error) {
                //TODO
            }
            else {
                //TODO
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
