package com.jbooktrader.platform.report;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

/**
 * Base implementation that avoid the need to implement the jettyLog interface
 * @author Florent Guiliani
 */
public abstract class AbstractReport implements Report {

    /// Jetty RequestLog interface
    public void log(Request request, Response response) {
        // Inspired from http://jetty.mortbay.org/xref/org/mortbay/jetty/NCSARequestLog.html
        StringBuilder buf = new StringBuilder();
        buf.append("Jetty ");
        buf.append(request.getServerName());
        buf.append(' ');
        buf.append(request.getRemoteAddr());
        buf.append(" - ");
        String user = request.getRemoteUser();
        buf.append((user == null)? " - " : user);
        buf.append(" [");
        buf.append(request.getTimeStampBuffer().toString());
        buf.append("] \"");
        buf.append(request.getMethod());
        buf.append(' ');
        buf.append(request.getUri());
        buf.append(' ');
        buf.append(request.getProtocol());
        buf.append("\" ");
        int status = response.getStatus();
        if (status<=0)
        {
            status=404;
        }
        buf.append(status);
        long responseLength=response.getContentCount();
        if (responseLength >=0)
        {
            buf.append(' ');
            buf.append(responseLength);
        }
        report(buf);
    }

    /// Jetty RequestLog interface, useless for JBT
    public boolean isFailed() {
        return false;
    }

    /// Jetty RequestLog interface, useless for JBT
    public boolean isRunning() {
        return true;
    }

    /// Jetty RequestLog interface, useless for JBT
    public boolean isStarted() {
        return true;
    }

    /// Jetty RequestLog interface, useless for JBT
    public boolean isStarting() {
        return false;
    }

    /// Jetty RequestLog interface, useless for JBT
    public boolean isStopped() {
        return false;
    }

    /// Jetty RequestLog interface, useless for JBT
    public boolean isStopping() {
        return false;
    }

    /// Jetty RequestLog interface, useless for JBT
    public void start() throws Exception {
    }

    /// Jetty RequestLog interface, useless for JBT
    public void stop() throws Exception {
    }
}