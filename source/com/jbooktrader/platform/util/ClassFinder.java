package com.jbooktrader.platform.util;

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
    public static List<String> getClasses() {
        URL[] classpath = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
        List<String> classNames = new ArrayList<String>();

        for (URL url : classpath) {
            File file;
            try {
                file = new File(url.toURI());
            } catch (URISyntaxException urise) {
                throw new RuntimeException(url + " is not a valid URI");
            }
            if (file.isDirectory()) {
                File packageDir = new File(file.getPath() + "/com/jbooktrader/strategy/");
                if (packageDir.exists()) {
                    for (File f : packageDir.listFiles()) {
                        String className = f.getName();
                        if (className.endsWith(".class")) {
                            className = className.substring(0, className.lastIndexOf(".class"));
                            classNames.add(className);
                        }
                    }
                }
            }
        }

        Collections.sort(classNames);
        return classNames;
    }

    public static Strategy getInstance(String name) throws JBookTraderException {
        try {
            String className = "com.jbooktrader.strategy." + name;
            Class<? extends Strategy> clazz = Class.forName(className).asSubclass(Strategy.class);
            if (Modifier.isAbstract(clazz.getModifiers())) {
                return null;
            }

            Class<?>[] parameterTypes = new Class[] {StrategyParams.class};
            Constructor<?> constructor = clazz.getConstructor(parameterTypes);
            return (Strategy) constructor.newInstance(new StrategyParams());

        } catch (ClassCastException cce) {
            throw new JBookTraderException("Class " + name + " does not extend class Strategy.");
        } catch (ClassNotFoundException cnte) {
            throw new JBookTraderException("Class " + name + " not found");
        } catch (Exception e) {
            throw new JBookTraderException(e.getCause().getMessage());
        }
    }


    public static List<Strategy> getStrategies() {
        List<Strategy> strategies = new ArrayList<Strategy>();
        List<String> strategyNames = getClasses();

        for (String strategyName : strategyNames) {
            try {
                Strategy strategy = getInstance(strategyName);
                if (strategy != null) {
                    strategies.add(strategy);
                }
            } catch (Exception e) {
                String msg = "Could not create strategy " + strategyName + ": ";
                msg += (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
                throw new RuntimeException(msg, e);
            }
        }

        return strategies;
    }
}
