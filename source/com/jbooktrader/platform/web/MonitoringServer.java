package com.jbooktrader.platform.web;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.ui.*;
import com.sun.net.httpserver.*;

import java.net.*;
import java.util.concurrent.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * @author Eugene Kononov
 */
public class MonitoringServer {
    private static HttpServer server;

    public static void start() {
        if (server == null) {
            PreferencesHolder prefs = PreferencesHolder.getInstance();
            if (prefs.get(WebAccess).equalsIgnoreCase("enabled")) {
                EventReport eventReport = Dispatcher.getInstance().getEventReport();
                try {
                    int port = Integer.parseInt(prefs.get(WebAccessPort));
                    server = HttpServer.create(new InetSocketAddress(port), 0);
                    HttpContext context = server.createContext("/", new WebHandler());
                    context.setAuthenticator(new WebAuthenticator());
                    server.setExecutor(Executors.newSingleThreadExecutor());
                    server.start();
                    eventReport.report(JBookTrader.APP_NAME, "Monitoring server started");
                } catch (Exception e) {
                    eventReport.report(e);
                    MessageDialog.showError("Could not start monitoring server: " + e);
                }
            }
        }
    }
}
