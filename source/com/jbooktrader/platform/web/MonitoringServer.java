package com.jbooktrader.platform.web;

import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.*;
import org.mortbay.jetty.*;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.nio.*;
import org.mortbay.jetty.security.*;
import org.mortbay.jetty.servlet.*;

public class MonitoringServer {
    private static Server server;
    private static final String ROLE = "admin";

    public static void start() throws JBookTraderException {

        if (server != null && server.isRunning()) {
            return;
        }

        PreferencesHolder prefs = PreferencesHolder.getInstance();
        boolean isEnabled = prefs.get(WebAccess).equalsIgnoreCase("enabled");

        if (!isEnabled) {
            return;
        }

        try {

            // Configure Jetty logging to use JBT EventReport.
            // see http://docs.codehaus.org/display/JETTY/Debugging
            System.setProperty("org.mortbay.log.class", "com.jbooktrader.platform.report.JettyLog");
            
            server = new Server();

            // Help on Handlers : http://jetty.mortbay.org/xref/org/mortbay/jetty/handler/package-summary.html
            
            // HandlerCollection: A collection of handlers.
            // For each request, all handler are called, regardless of
            // the response status or exceptions.
            HandlerCollection handlers = new HandlerCollection();

            // Create the log handler and add it to the HandlerCollection
            RequestLogHandler requestLogHandler = new RequestLogHandler();
            requestLogHandler.setRequestLog( Dispatcher.getReporter() );
            handlers.addHandler(requestLogHandler);

            // Create the security handler but don't add it to the HandlerCollection.
            // It will be added in the ContextHandler below.
            SecurityHandler securityHandler = new SecurityHandler();
            ConstraintMapping constraintMapping = new ConstraintMapping();
            Constraint constraint = new Constraint(Constraint.__BASIC_AUTH, ROLE);
            constraint.setAuthenticate(true);
            constraintMapping.setConstraint(constraint);
            constraintMapping.setPathSpec("/*");
            securityHandler.setConstraintMappings(new ConstraintMapping[]{constraintMapping});
            String userName = prefs.get(WebAccessUser);
            String password = prefs.get(WebAccessPassword);
            HashUserRealm userRealm = new HashUserRealm(JBookTrader.APP_NAME);
            userRealm.addUserToRole(userName, ROLE);
            userRealm.put(userName, password);
            securityHandler.setUserRealm(userRealm);

            // Create a Context handler, this is a ContextHandler helper, and add it to the HandlerCollection.
            // This helper handler will contain the SecurityHandler plus:
            //   - a sessionHandler
            //   - a servletHandler
            //   - an errorHandler
            Context context = new Context(handlers, "/", Context.SESSIONS);
            context.setSecurityHandler(securityHandler);
            context.addServlet(new ServletHolder(new JBTServlet()), "/*");
            
            // registers the HandlerCollection into the server 
            server.setHandler(handlers);

            // Create the connector aka the server socket
            Connector connector;
            if(prefs.get(JBTPreferences.WebAccessHTTPS).equals("enabled")) {
                SslSelectChannelConnector sslconnector = new SslSelectChannelConnector();
                sslconnector.setKeystore(prefs.get(JBTPreferences.SSLkeystore));
                sslconnector.setPassword(prefs.get(JBTPreferences.SSLkeystorePassword));
                sslconnector.setKeyPassword(prefs.get(JBTPreferences.SSLkeyPassword));
                connector = sslconnector;
            }
            else {
                connector = new SelectChannelConnector();
            }

            connector.setPort(Integer.parseInt(prefs.get(WebAccessPort)));
                        
            server.setConnectors(new Connector[]{connector});

            // Tune and start the Embedded Jetty HTTP Server 
            server.setSendServerVersion(false);
            server.start();
        }
        catch (Exception e) {
            Dispatcher.getReporter().report(e);
            MessageDialog.showError(null, "Could not start monitoring server: " + e.getMessage());
        }
    }
}
