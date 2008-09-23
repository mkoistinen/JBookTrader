package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.model.Dispatcher.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.io.*;

public class CommandLineBackTester {
    private final PreferencesHolder prefs = PreferencesHolder.getInstance();

    private class CommandLineBackTesterProgressIndicator implements BackTestProgressIndicator {

        public void dispose() {
            System.err.println("Finished.");
        }

        public void enableProgress() {
            System.err.println("Starting to backtest...");
        }

        public void setProgress(long count, long iterations, String text) {
            System.err.println(count + " out of " + iterations + " made. " + text);
        }

        public void showProgress(String progressText) {
            System.err.println(progressText);
        }

    }

    public CommandLineBackTester(String strategyName, String dataFileName) throws JBookTraderException, InterruptedException {

        File file = new File(dataFileName);
        if (!file.exists()) {
            throw new JBookTraderException("Historical file " + "\"" + dataFileName + "\"" + " does not exist.");
        }

        prefs.set(BackTesterFileName, dataFileName);
        Dispatcher.setMode(Mode.BackTest);

        Strategy strategy = ClassFinder.getInstance(strategyName);
        BackTestStrategyRunner bts = new BackTestStrategyRunner(new CommandLineBackTesterProgressIndicator(), strategy, dataFileName);
        bts.run();
    }
}
