package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.JBookTraderException;

import java.io.*;
import java.util.Properties;

public class PreferencesHolder {
    private static final String userHome = System.getProperty("user.home");
    private static final String propertiesFileName = userHome + "/JBookTrader.preferences";
    private final Properties properties;
    private static PreferencesHolder instance;

    public static synchronized PreferencesHolder getInstance() throws JBookTraderException {
        if (instance == null) {
            instance = new PreferencesHolder();
        }
        return instance;
    }

    // private constructor for non-instantiability
    private PreferencesHolder() throws JBookTraderException {
        properties = new Properties();

        File file = new File(propertiesFileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ioe) {
                throw new JBookTraderException("Could not create pereferences file " + propertiesFileName);
            }
        }

        try {
            FileInputStream fis = new FileInputStream(propertiesFileName);
            properties.load(fis);
            fis.close();
        } catch (Exception e) {
            throw new JBookTraderException("Could not load file " + propertiesFileName);
        }
    }

    public String getProperty(String propertyName) {
        String property = properties.getProperty(propertyName);
        if (property == null) {
            property = "";
        }
        return property;
    }

    public int getPropertyAsInt(String propertyName) {
        int value = -1;
        String property = properties.getProperty(propertyName);
        if (property != null) {
            value = Integer.valueOf(property);
        }

        return value;
    }

    public void setProperty(String propertyName, String propertyValue) throws JBookTraderException {
        try {
            properties.setProperty(propertyName, propertyValue);
            OutputStream out = new FileOutputStream(propertiesFileName);
            properties.store(out, "JBookTrader User Preferences");
            out.flush();
            out.close();
        } catch (Exception e) {
            throw new JBookTraderException("Could not write file " + propertiesFileName);
        }
    }

    public void setProperty(String propertyName, long propertyValue) throws JBookTraderException {
        try {
            setProperty(propertyName, String.valueOf(propertyValue));
        } catch (Exception e) {
            throw new JBookTraderException("Could not write file " + propertiesFileName);
        }
    }
}
