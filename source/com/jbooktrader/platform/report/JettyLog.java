package com.jbooktrader.platform.report;

import org.mortbay.log.Logger;

import com.jbooktrader.platform.model.Dispatcher;

/**
 * Inspired from http://www.mortbay.org/jetty/jetty-6/xref/org/mortbay/log/StdErrLog.html
 * @author Florent Guiliani 
 */
public class JettyLog implements Logger {

    private static boolean debug = System.getProperty("DEBUG",null)!=null;
    private Report report;

    public JettyLog()
    {
        report = Dispatcher.getReporter();
    }

    public boolean isDebugEnabled()
    {
        return debug;
    }

    public void setDebugEnabled(boolean enabled)
    {
        debug=enabled;
    }

    public void info(String msg,Object arg0, Object arg1)
    {
        report.report("Jetty:INFO:  "+format(msg,arg0,arg1));
    }

    public void debug(String msg,Throwable th)
    {
        if (debug)
        {
            report.report("Jetty:DEBUG: "+msg);
            if (th!=null)
                report.report(th);
        }
    }

    public void debug(String msg,Object arg0, Object arg1)
    {
        if (debug)
        {
            report.report("Jetty:DEBUG: "+format(msg,arg0,arg1));
        }
    }

    public void warn(String msg,Object arg0, Object arg1)
    {
        report.report("Jetty:WARN:  "+format(msg,arg0,arg1));
    }

    public void warn(String msg, Throwable th)
    {
        report.report("Jetty:WARN:  "+msg);
        if (th!=null)
            report.report(th);
    }

    private String format(String msg, Object arg0, Object arg1)
    {
        int i0=msg.indexOf("{}");
        int i1=i0<0?-1:msg.indexOf("{}",i0+2);

        if (arg1!=null && i1>=0)
            msg=msg.substring(0,i1)+arg1+msg.substring(i1+2);
        if (arg0!=null && i0>=0)
            msg=msg.substring(0,i0)+arg0+msg.substring(i0+2);
        return msg;
    }

    public Logger getLogger(String name)
    {
        return this;
    }

    public String toString()
    {
        return report.toString();
    }

}
