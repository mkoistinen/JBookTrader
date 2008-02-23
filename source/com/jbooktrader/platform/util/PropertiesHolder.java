package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.JBookTraderException;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class PropertiesHolder {
    private static final String propertiesFileName = "JBookTrader.properties";
    private final Properties properties;
    private static PropertiesHolder instance;

    public static synchronized PropertiesHolder getInstance() throws JBookTraderException {
        if (instance == null) {
            instance = new PropertiesHolder();
        }
        return instance;
    }

    // private constructor for non-instantiability 
    private PropertiesHolder() throws JBookTraderException {
        properties = new Properties();
        URL url = ClassLoader.getSystemResource(propertiesFileName);
        if (url == null) {
            throw new JBookTraderException("Could not find file " + propertiesFileName);
        }

        try {
            InputStream fis = ClassLoader.getSystemResourceAsStream(propertiesFileName);
            properties.load(fis);
            fis.close();
        } catch (Exception e) {
            throw new JBookTraderException("Could not load file " + propertiesFileName);
        }
    }

    public String getProperty(String propertyName) throws JBookTraderException {
        String property = properties.getProperty(propertyName);
        if (property == null) {
            throw new JBookTraderException("Property \"" + propertyName + "\"" + " is not found in the properties file.");
        }
        return property;
    }

    public String[] getPropertyAsStringArray(String propertyName) throws JBookTraderException {
        String property = properties.getProperty(propertyName);
        if (property == null) {
            throw new JBookTraderException("Property \"" + propertyName + "\"" + " is not found in the properties file");
        }
        return toStringArray(property);
    }

    private static String[] toStringArray(String s) {
        StringTokenizer st = new StringTokenizer(s, ",");
        List<String> items = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            items.add(st.nextToken());
        }

        return items.toArray(new String[items.size()]);
    }

}
