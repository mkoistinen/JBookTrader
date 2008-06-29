package com.jbooktrader.platform.util;

import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.strategy.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;


public class ClassFinder {

    /**
     * Searches the classpath (including inside the JAR files) to find classes
     * that extend the specified superclass. The intent is to be able to implement
     * new strategy classes as "plug-and-play" units of JBookTrader. That is,
     * JBookTrader will know how to run a trading strategy as long as that
     * strategy is implemented in a class that extends the base Strategy class.
     */
    private List<String> getClasses(String packageName) throws URISyntaxException {
        URL[] classpath = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
        List<String> classNames = new ArrayList<String>();

        for (URL url : classpath) {
            File file = new File(url.toURI());
            if (file.isDirectory()) {
                File packageDir = new File(file.getPath() + '/' + packageName);
                if (packageDir.exists()) {
                    for (File f : packageDir.listFiles()) {
                        String className = packageName + f.getName().substring(0, f.getName().lastIndexOf('.'));
                        className = className.replace('/', '.');
                        classNames.add(className);
                    }
                }
            }
        }

        Collections.sort(classNames);
        return classNames;
    }

    public static Strategy getInstance(String name) throws JBookTraderException {
        try {
            Class<? extends Strategy> clazz = Class.forName(name).asSubclass(Strategy.class);
            Class<?>[] parameterTypes = new Class[]{StrategyParams.class, MarketBook.class};
            Constructor<?> constructor = clazz.getConstructor(parameterTypes);
            return (Strategy) constructor.newInstance(new StrategyParams(), new MarketBook());
        } catch (ClassCastException cce) {
            throw new JBookTraderException("Class " + name + " does not extend Strategy.");
        } catch (Exception e) {
            throw new JBookTraderException(e.getCause().getMessage());
        }
    }

    public List<Strategy> getStrategies() throws JBookTraderException {
        List<Strategy> strategies = new ArrayList<Strategy>();
        List<String> strategyNames;
        try {
            strategyNames = getClasses("com/jbooktrader/strategy/");
        } catch (Exception e) {
            throw new JBookTraderException(e);
        }

        for (String strategyName : strategyNames) {
            try {
                Strategy strategy = getInstance(strategyName);
                strategies.add(strategy);
            } catch (Exception e) {
                String msg = "Could not create strategy " + strategyName + ": ";
                msg += (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
                throw new JBookTraderException(msg);
            }
        }
        return strategies;
    }
}
