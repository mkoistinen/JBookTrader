package com.jbooktrader.platform.web;

import java.io.FileInputStream;

import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.util.*;
import org.mortbay.jetty.*;
import org.mortbay.jetty.servlet.*;
import org.mortbay.xml.XmlConfiguration;

public class MonitoringServer {
    private static boolean isStarted;

    public static void start() throws JBookTraderException {
        if (isStarted)
            return;

        try {
            Server server = new Server();
            XmlConfiguration configuration = new XmlConfiguration(new FileInputStream(JBookTrader.getAppPath()+"/conf/jetty.xml"));
            configuration.configure(server);
            Context context = new Context(server, "/", Context.SESSIONS);
            context.addServlet(new ServletHolder(new JBTServlet()), "/*");
            server.start();
            isStarted = true;
        }
        catch (Exception e) {
            Dispatcher.getReporter().report(e);
            MessageDialog.showError(null, "Could not start monitoring server: " + e.getMessage());
        }
    }
}
