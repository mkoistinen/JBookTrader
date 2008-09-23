package com.jbooktrader.platform.web;

import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.util.*;
import org.mortbay.jetty.*;
import org.mortbay.jetty.servlet.*;

public class MonitoringServer {
    private static boolean isStarted;

    public static void start() throws JBookTraderException {
        if (isStarted)
            return;

        try {
            Server server = new Server(1234);
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
