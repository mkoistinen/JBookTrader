package com.jbooktrader.platform.preferences;

import java.util.prefs.*;

/**
 * @author Eugene Kononov
 */
public class PreferencesHolder {
    private static PreferencesHolder instance;
    private final Preferences prefs;

    public static synchronized PreferencesHolder getInstance() {
        if (instance == null) {
            instance = new PreferencesHolder();
        }
        return instance;
    }

    // private constructor for non-instantiability
    private PreferencesHolder() {
        prefs = Preferences.userNodeForPackage(getClass());
    }

    public int getInt(JBTPreferences pref) {
        String value = get(pref);
        return Integer.valueOf(value);
    }

    public String get(JBTPreferences pref) {
        return prefs.get(pref.getName(), pref.getDefault());
    }

    public void set(JBTPreferences pref, Object propertyValue) {
        prefs.put(pref.getName(), String.valueOf(propertyValue));
    }
}
