package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;

import java.util.concurrent.*;

public class HeartBeatSender {

    class Sender implements Runnable {
        public void run() {
            Dispatcher.Mode mode = Dispatcher.getMode();
            if (mode == Trade || mode == ForwardTest) {
                String message = "Heart beat message: ";
                boolean isConnected = Dispatcher.getTrader().getAssistant().isConnected();
                if (isConnected) {
                    message += "JBT operating normally.";
                } else {
                    message += "TWS was disconnected from IB server.";
                }
                SecureMailSender.getInstance().send(message);
            }
        }
    }

    public HeartBeatSender() {
        PreferencesHolder prefs = PreferencesHolder.getInstance();
        if (prefs.get(EmailMonitoring).equalsIgnoreCase("enabled")) {
            int minutes = Integer.parseInt(prefs.get(HeartBeatInterval));
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleWithFixedDelay(new Sender(), 0, minutes * 60, TimeUnit.SECONDS);
        }
    }
}

