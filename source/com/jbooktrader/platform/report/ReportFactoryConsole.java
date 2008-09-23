package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.*;

import java.io.*;

public class ReportFactoryConsole implements ReportFactory {

    public Report newReport(String fileName) throws JBookTraderException {
        return new Report(new TextReportRenderer(), new PrintWriter(System.out));
    }

}
