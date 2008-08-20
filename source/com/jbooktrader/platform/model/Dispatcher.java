package com.jbooktrader.platform.model;


import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.trader.*;

import java.io.*;
import java.util.*;

/**
 * Acts as the dispatcher of the services.
 */
public class Dispatcher {
    public enum Mode {
        Trade, BackTest, ForwardTest, Optimization
    }

    private static final List<ModelListener> listeners = new ArrayList<ModelListener>();
    private static Report eventReport;
    private static Trader trader;
    private static Mode mode;
    private static int activeStrategies;

    public static void setReporter(String eventReportFileName) throws IOException, JBookTraderException {
        eventReport = new Report(eventReportFileName);
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

    public static Report getReporter() {
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
        eventReport.report("Mode set to: " + mode);

        // Disable all reporting when JBT runs in optimization mode. The optimizer runs
        // thousands of strategies, and the amount of data to report would be enormous.
        if (mode == Mode.Optimization) {
            Report.disable();
        } else {
            Report.enable();
        }

        if (mode == Mode.Trade || mode == Mode.ForwardTest) {
            trader.getAssistant().connect();
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
