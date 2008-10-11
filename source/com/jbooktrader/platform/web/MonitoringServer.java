package com.jbooktrader.platform.web;

import com.jbooktrader.platform.model.Dispatcher;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.*;
import org.mortbay.jetty.*;
import org.mortbay.jetty.nio.*;
import org.mortbay.jetty.security.*;
import org.mortbay.jetty.servlet.*;

public class MonitoringServer {
    private static Server server;
    private static final String ROLE = "admin";

    public static void start() {

        if (server != null && server.isRunning()) {
            return;
        }

        PreferencesHolder prefs = PreferencesHolder.getInstance();
        boolean isEnabled = prefs.get(WebAccess).equalsIgnoreCase("enabled");

        if (!isEnabled) {
            return;
        }

        try {

            server = new Server();

            SecurityHandler securityHandler = new SecurityHandler();
            ConstraintMapping constraintMapping = new ConstraintMapping();
            Constraint constraint = new Constraint(Constraint.__BASIC_AUTH, ROLE);
            constraint.setAuthenticate(true);
            constraintMapping.setConstraint(constraint);
            constraintMapping.setPathSpec("/*");
            securityHandler.setConstraintMappings(new ConstraintMapping[]{constraintMapping});


            int port = Integer.parseInt(prefs.get(WebAccessPort));
            String userName = prefs.get(WebAccessUser);
            String password = prefs.get(WebAccessPassword);


            HashUserRealm userRealm = new HashUserRealm(JBookTrader.APP_NAME);
            userRealm.addUserToRole(userName, ROLE);
            userRealm.put(userName, password);
            securityHandler.setUserRealm(userRealm);
            server.setHandler(securityHandler);

            Connector connector = new SelectChannelConnector();
            connector.setPort(port);
            server.setConnectors(new Connector[]{connector});

            Context context = new Context(server, "/", Context.SESSIONS);
            context.addServlet(new ServletHolder(new JBTServlet()), "/*");

            server.setSendServerVersion(false);
            server.start();
        }
        catch (Exception e) {
            Dispatcher.getReporter().report(e);
            MessageDialog.showError(null, "Could not start monitoring server: " + e.getMessage());
        }
    }
}
