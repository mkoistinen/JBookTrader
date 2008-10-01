package com.jbooktrader.platform.report;

import java.util.LinkedList;
import java.util.List;

/**
 * A report that brodcast reports event to many report instances
 * @author Florent Guiliani
 */
public class MultiReport extends AbstractReport {

    private LinkedList<Report> reports = new LinkedList<Report>();
    
    public MultiReport() {
        
    }
    
    public void addReport(Report report) {
        reports.add(report);
    }
    
    public void report(StringBuilder message) {
        for(Report report: reports) {
            report.report(message);
        }            
    }

    public void report(String message) {
        for(Report report: reports) {
            report.report(message);
        }            
    }

    public void report(Throwable t) {
        for(Report report: reports) {
            report.report(t);
        }            
    }

    public void report(List<?> columns) {
        for(Report report: reports) {
            report.report(columns);
        }            
    }

    public void report(List<?> columns, String time) {
        for(Report report: reports) {
            report.report(columns, time);
        }            
    }

    public void reportDescription(String message) {
        for(Report report: reports) {
            report.reportDescription(message);
        }            
    }
}
