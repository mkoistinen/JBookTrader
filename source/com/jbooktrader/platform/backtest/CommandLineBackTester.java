package com.jbooktrader.platform.backtest;

import java.io.File;

import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ClassFinder;
import com.jbooktrader.platform.util.MessageDialog;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;

public class CommandLineBackTester {
    private final PreferencesHolder prefs;
    
    private class CommandLineBackTesterProgressIndicator implements BackTestProgressIndicator {

		public void dispose() {
			System.err.println("Finished.");	
		}

		public void enableProgress() {
			System.err.println("Starting to backtest...");
		}

		public void setProgress(long count, long iterations, String text) {
			System.err.println(count + " out of "+iterations+" made. "+text);
		}

		public void showProgress(String progressText) {
			System.out.println(progressText);	
		}
    	
    }
    
	public CommandLineBackTester(String strategyName, String dataFileName) {

    	prefs = PreferencesHolder.getInstance();
        try {
            File file = new File(dataFileName);
            if (!file.exists()) {
                throw new JBookTraderException("Historical file " + "\"" + dataFileName + "\"" + " does not exist.");
            }

            prefs.set(BackTesterFileName, dataFileName);
            Dispatcher.setMode(BackTest);

            Strategy strategy = ClassFinder.getInstance(strategyName);
            Thread bts = new Thread(new BackTestStrategyRunner(new CommandLineBackTesterProgressIndicator(), strategy, dataFileName));
            bts.start();
            bts.join();
        } catch (Exception ex) {
            MessageDialog.showError(null, ex.getMessage());
        }

	}
}
