package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.*;

import java.io.*;

/**
 * Create a report that write events both on standard ouput and into the reports directory
 * @author Florent Guiliani
 */
public class ReportFactoryConsole implements ReportFactory {

    public Report newReport(String fileName) throws JBookTraderException {
        MultiReport multireport = new MultiReport();
        multireport.addReport(new SingleReport(new TextReportRenderer(), new PrintWriter(System.out)));
        multireport.addReport(new ReportFactoryFile().newReport(fileName));
        return multireport;
    }

}
