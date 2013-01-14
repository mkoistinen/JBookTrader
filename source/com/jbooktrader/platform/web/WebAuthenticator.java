package com.jbooktrader.platform.web;

import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.startup.*;
import com.sun.net.httpserver.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * @author Eugene Kononov
 */
public class WebAuthenticator extends BasicAuthenticator {
    private final String authPair;

    public WebAuthenticator() {
        super(JBookTrader.APP_NAME);
        PreferencesHolder prefs = PreferencesHolder.getInstance();
        authPair = prefs.get(WebAccessUser) + "/" + prefs.get(WebAccessPassword);
    }

    @Override
    public boolean checkCredentials(String userName, String password) {
        return authPair.equals(userName + "/" + password);
    }
}