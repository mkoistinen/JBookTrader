package com.jbooktrader.platform.report;

import java.util.List;

import org.mortbay.jetty.RequestLog;

public interface Report extends RequestLog {

    public void report(StringBuilder message);
    public void report(String message);
    public void report(Throwable t);
    public void reportDescription(String message);
    public void report(List<?> columns);
    public void report(List<?> columns, String time);
}
