package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;
import java.util.concurrent.*;

public class HeartBeatSender {
    private final List<Strategy> strategies;
    private static HeartBeatSender instance;

    class Sender implements Runnable {
        public void run() {
            long instant = System.currentTimeMillis();
            boolean isInTradingPeriod = false;
            for (Strategy strategy : strategies) {
                if (strategy.getTradingSchedule().contains(instant)) {
                    isInTradingPeriod = true;
                    break;
                }
            }

            if (isInTradingPeriod) {
                Dispatcher.Mode mode = Dispatcher.getMode();
                if (mode == Trade || mode == ForwardTest) {
                    String message = "Heart beat message: ";
                    boolean isConnected = Dispatcher.getTrader().getAssistant().isConnected();
                    if (isConnected) {
                        message += "JBT is operating normally.";
                    } else {
                        message += "TWS was disconnected from IB server.";
                    }
                    SecureMailSender.getInstance().send(message);
                }
            }
        }
    }

    public synchronized static HeartBeatSender getInstance() {
        if (instance == null) {
            instance = new HeartBeatSender();
        }

        return instance;
    }


    public void addStrategy(Strategy strategy) {
        strategies.add(strategy);
    }

    // private constructor for non-instantiability
    private HeartBeatSender() {
        strategies = new ArrayList<Strategy>();
        PreferencesHolder prefs = PreferencesHolder.getInstance();
        if (prefs.get(EmailMonitoring).equalsIgnoreCase("enabled")) {
            int frequencyMinutes = Integer.parseInt(prefs.get(HeartBeatInterval));
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            long instant = System.currentTimeMillis();
            long periodLengthInMillis = frequencyMinutes * 60 * 1000;
            // Integer division gives us the number of whole periods
            long completedPeriods = instant / periodLengthInMillis;
            long firstNotificationTime = (completedPeriods + 1) * periodLengthInMillis;
            long initialDelay = (firstNotificationTime - instant) / 1000;

            scheduler.scheduleWithFixedDelay(new Sender(), initialDelay, frequencyMinutes * 60, TimeUnit.SECONDS);
        }
    }
}

