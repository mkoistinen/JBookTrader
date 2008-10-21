package com.jbooktrader.platform.web;

import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.util.*;
import com.sun.net.httpserver.*;

import java.net.*;
import java.util.concurrent.*;

public class MonitoringServer {
    private static HttpServer server;

    public static void start() {
        if (server == null) {
            PreferencesHolder prefs = PreferencesHolder.getInstance();
            if (prefs.get(WebAccess).equalsIgnoreCase("enabled")) {
                try {
                    int port = Integer.parseInt(prefs.get(WebAccessPort));
                    server = HttpServer.create(new InetSocketAddress(port), 0);
                    HttpContext context = server.createContext("/", new WebHandler());
                    context.setAuthenticator(new WebAuthenticator());
                    server.setExecutor(Executors.newSingleThreadExecutor());
                    server.start();
                    Dispatcher.getReporter().report("Monitoring server started");
                } catch (Exception e) {
                    Dispatcher.getReporter().report(e);
                    MessageDialog.showError(null, "Could not start monitoring server: " + e.getMessage());
                }
            }
        }
    }
}
