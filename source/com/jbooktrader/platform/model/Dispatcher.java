package com.jbooktrader.platform.model;


import com.jbooktrader.platform.model.ModelListener.*;
import com.jbooktrader.platform.portfolio.manager.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.trader.*;
import com.jbooktrader.platform.util.ntp.*;
import com.jbooktrader.platform.web.*;

import java.io.*;
import java.util.*;

/**
 * Acts as the dispatcher of the services.
 *
 * @author Eugene Kononov
 */
public class Dispatcher {
    private static Dispatcher instance;
    private final List<ModelListener> listeners;
    private EventReport eventReport;
    private Trader trader;
    private PortfolioManager portfolioManager;
    private NTPClock ntpClock;
    private Mode mode;
    private String reportsDir, marketDataDir, resourcesDir;

    private Dispatcher() {
        listeners = new ArrayList<>();
    }

    public void init(String homeDir) throws IOException {
        reportsDir = homeDir + "/reports/";
        File reportsDirFile = new File(reportsDir);
        if (!reportsDirFile.exists()) {
            boolean isCreated = reportsDirFile.mkdir();
            if (!isCreated) {
                throw new RuntimeException("Could not create directory " + reportsDir);
            }
        }

        marketDataDir = homeDir + "/marketData/";
        File marketDataDirFile = new File(marketDataDir);
        if (!marketDataDirFile.exists()) {
            boolean isCreated = marketDataDirFile.mkdir();
            if (!isCreated) {
                throw new RuntimeException("Could not create directory " + marketDataDir);
            }
        }

        resourcesDir = homeDir + "/resources/";

        eventReport = new EventReport();
    }


    public static synchronized Dispatcher getInstance() {
        if (instance == null) {
            instance = new Dispatcher();
        }
        return instance;
    }

    public String getReportsDir() {
        return reportsDir;
    }

    public String getMarketDataDir() {
        return marketDataDir;
    }

    public String getResourcesDir() {
        return resourcesDir;
    }

    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ModelListener listener) {
        listeners.remove(listener);
    }

    public void fireModelChanged(Event event, Object value) {
        for (ModelListener listener : listeners) {
            try {
                listener.modelChanged(event, value);
            } catch (Exception e) {
                eventReport.report(e);
            }
        }
    }

    public void fireModelChanged(Event event) {
        fireModelChanged(event, null);
    }


    public synchronized Trader getTrader() {
        if (trader == null) {
            trader = new Trader();
        }
        return trader;
    }

    public synchronized PortfolioManager getPortfolioManager() {
        if (portfolioManager == null) {
            portfolioManager = new PortfolioManager();
        }
        return portfolioManager;
    }


    public NTPClock getNTPClock() {
        return ntpClock;
    }

    public EventReport getEventReport() {
        return eventReport;
    }

    public Mode getMode() {
        return mode;
    }

    public void exit() {
        if (trader != null) {
            trader.getAssistant().disconnect();
        }
        System.exit(0);
    }

    public void setMode(Mode mode) throws JBookTraderException {
        if (mode == Mode.Trade || mode == Mode.ForwardTest) {
            if (ntpClock == null) {
                ntpClock = new NTPClock();
            }
        }

        if (this.mode != mode) {
            eventReport.report(JBookTrader.APP_NAME, "Running mode changed to: " + mode.getName());
        }

        this.mode = mode;

        // Disable all reporting when JBT runs in optimization mode. The optimizer runs
        // thousands of strategies, and the amount of data to report would be enormous.
        if (mode == Mode.Optimization) {
            eventReport.disable();
        } else {
            eventReport.enable();
        }

        if (mode == Mode.Trade || mode == Mode.ForwardTest || mode == Mode.ForceClose) {
            trader.getAssistant().connect();
            MonitoringServer.start();
        } else {
            trader.getAssistant().disconnect();
        }

        fireModelChanged(Event.ModeChanged);
    }
}
