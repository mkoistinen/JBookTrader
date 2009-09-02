package com.jbooktrader.platform.model;


import com.jbooktrader.platform.c2.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.trader.*;
import com.jbooktrader.platform.web.*;

import java.util.*;

/**
 * Acts as the dispatcher of the services.
 */
public class Dispatcher {

    public enum Mode {
        Trade("Trading"),
        BackTest("Back Testing"),
        ForwardTest("Forward Testing"),
        Optimization("Optimizing");

        private final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }


    private static final List<ModelListener> listeners = new ArrayList<ModelListener>();
    private static EventReport eventReport;
    private static Trader trader;
    private static C2Manager c2Manager;
    private static Mode mode;
    private static int activeStrategies;

    public static void setReporter() throws JBookTraderException {
        eventReport = new EventReport("EventReport");
    }

    public static void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(ModelListener listener) {
        listeners.remove(listener);
    }

    public static void fireModelChanged(ModelListener.Event event, Object value) {
        if (mode != Mode.Optimization) {
            for (ModelListener listener : listeners) {
                try {
                    listener.modelChanged(event, value);
                } catch (Exception e) {
                    eventReport.report(e);
                }
            }
        }
    }

    synchronized public static Trader getTrader() {
        if (trader == null) {
            trader = new Trader();
        }
        return trader;
    }

    synchronized public static C2Manager getC2Manager() {
        if (c2Manager == null) {
            c2Manager = new C2Manager();
        }
        return c2Manager;
    }


    public static EventReport getEventReport() {
        return eventReport;
    }

    public static Mode getMode() {
        return mode;
    }

    public static void exit() {
        if (trader != null) {
            trader.getAssistant().disconnect();
        }
        System.exit(0);
    }

    public static void setMode(Mode mode) throws JBookTraderException {
        Dispatcher.mode = mode;
        eventReport.report("Running mode: " + mode.getName());

        // Disable all reporting when JBT runs in optimization mode. The optimizer runs
        // thousands of strategies, and the amount of data to report would be enormous.
        if (mode == Mode.Optimization) {
            EventReport.disable();
        } else {
            EventReport.enable();
        }

        if (mode == Mode.Trade || mode == Mode.ForwardTest) {
            trader.getAssistant().connect();
            MonitoringServer.start();
        } else {
            trader.getAssistant().disconnect();
        }

        fireModelChanged(ModelListener.Event.ModeChanged, null);
    }

    public static synchronized void strategyStarted() {
        activeStrategies++;
        fireModelChanged(ModelListener.Event.StrategiesStart, null);
    }

    public static synchronized void strategyCompleted() {
        activeStrategies--;
        if (activeStrategies == 0) {
            fireModelChanged(ModelListener.Event.StrategiesEnd, null);
        }
    }
}
