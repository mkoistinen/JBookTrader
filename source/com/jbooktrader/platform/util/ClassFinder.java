package com.jbooktrader.platform.util;

import com.jbooktrader.platform.marketdepth.MarketBook;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.strategy.Strategy;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.*;
import java.util.*;
import java.util.jar.*;


public class ClassFinder {

    /**
     * Searches the classpath (including inside the JAR files) to find classes
     * that extend the specified superclass. The intent is to be able to implement
     * new strategy classes as "plug-and-play" units of JBookTrader. That is,
     * JBookTrader will know how to run a trading strategy as long as that
     * strategy is implemented in a class that extends the base Strategy class.
     */
    private List<Class<?>> getClasses(String packageName, String superClassName) throws URISyntaxException, IOException, ClassNotFoundException {

        String packagePath = packageName.replace('.', '/');
        URL[] classpath = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
        List<Class<?>> classes = new ArrayList<Class<?>>();

        for (URL url : classpath) {
            List<String> classNames = new ArrayList<String>();

            ClassLoader classLoader = new URLClassLoader(new URL[]{url});
            URI uri = url.toURI();
            File file = new File(uri);

            if (file.getPath().endsWith(".jar")) {
                if (file.exists()) {
                    JarFile jarFile = new JarFile(file);
                    for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                        String entryName = (entries.nextElement()).getName();
                        if (entryName.matches(packagePath + "/\\w*\\.class")) {// get only class files in package dir
                            String className = entryName.replace('/', '.').substring(0, entryName.lastIndexOf('.'));
                            classNames.add(className);
                        }
                    }
                }
            } else {// directory
                File packageDirectory = new File(file.getPath() + "/" + packagePath);
                if (packageDirectory.exists()) {
                    for (File f : packageDirectory.listFiles()) {
                        if (f.getPath().endsWith(".class")) {
                            String className = packageName + "." + f.getName()
                                    .substring(0, f.getName().lastIndexOf('.'));
                            classNames.add(className);
                        }
                    }
                }
            }

            // make sure the strategy extends the base Strategy class
            for (String className : classNames) {
                Class<?> clazz = classLoader.loadClass(className);
                if (clazz.getSuperclass().getName().equals(superClassName)) {
                    classes.add(clazz);
                }
            }
        }

        Collections.sort(classes, new Comparator<Class<?>>() {
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return classes;
    }


    public List<Strategy> getStrategies() throws JBookTraderException {
        List<Strategy> strategies = new ArrayList<Strategy>();
        List<Class<?>> strategyClasses;

        try {
            strategyClasses = getClasses("com.jbooktrader.strategy", "com.jbooktrader.platform.strategy.Strategy");
        } catch (Exception e) {
            throw new JBookTraderException(e);
        }

        for (Class<?> strategyClass : strategyClasses) {
            try {
                Class<?>[] parameterTypes = new Class[]{StrategyParams.class, MarketBook.class};
                Constructor<?> constructor = strategyClass.getConstructor(parameterTypes);
                Strategy strategy = (Strategy) constructor.newInstance(new StrategyParams(), new MarketBook());
                strategies.add(strategy);
            } catch (Exception e) {
                String msg = "Could not create strategy " + strategyClass.getSimpleName() + ": ";
                msg += (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
                throw new JBookTraderException(msg);
            }
        }
        return strategies;
    }
}
