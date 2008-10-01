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
    private static List<String> getClasses(String packageName) throws JBookTraderException {
        URL[] classpath = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
        List<String> classNames = new ArrayList<String>();

        for (URL url : classpath) {
            File file;
            try {
                file = new File(url.toURI());
            } catch (URISyntaxException urise) {
                throw new JBookTraderException(url + " is not a valid URI");
            }
            if (file.isDirectory()) {
                File packageDir = new File(file.getPath() + '/' + packageName);
                if (packageDir.exists()) {
                    for (File f : packageDir.listFiles()) {
                        String className = f.getName();
                        int extIndex = className.lastIndexOf(".class");
                        if(extIndex>0) {
                            className = className.substring(0, extIndex);
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
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                Class<?>[] parameterTypes = new Class[]{StrategyParams.class};
                Constructor<?> constructor = clazz.getConstructor(parameterTypes);
                return (Strategy) constructor.newInstance(new StrategyParams());
            } else {
                return null;
            }
        } catch (ClassCastException cce) {
            throw new JBookTraderException("Class " + name + " does not extend Strategy.");
        } catch (ClassNotFoundException cnte) {
            throw new JBookTraderException("Class " + name + "not found");
        } catch (Exception e) {
            throw new JBookTraderException(e.getCause().getMessage());
        }
    }

    public static List<Strategy> getStrategies() throws JBookTraderException {
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
                if (strategy != null) {
                    strategies.add(strategy);
                }
            } catch (Exception e) {
                String msg = "Could not create strategy " + strategyName + ": ";
                msg += (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
                throw new JBookTraderException(msg);
            }
        }
        return strategies;
    }

    public static Vector<String> getReportRenderers() throws JBookTraderException {
        Vector<String> reportNames = new Vector<String>();

        for (String className : getClasses("com/jbooktrader/platform/report")) {
            try {
                String fullClassName = "com.jbooktrader.platform.report." + className;
                Class<?> clazz = Class.forName(fullClassName);
                boolean interfaceFound = false;
                for (Class<?> implementedInterface : clazz.getInterfaces()) {
                    if (implementedInterface.getName().equals("com.jbooktrader.platform.report.ReportRenderer")) {
                        interfaceFound = true;
                        break;
                    }
                }
                if (interfaceFound) {
                    reportNames.add(fullClassName);
                }
            } catch (Exception e) {
            }
        }

        return reportNames;
    }
}
