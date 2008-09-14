package com.jbooktrader.platform.report;

import java.io.PrintWriter;

import com.jbooktrader.platform.model.JBookTraderException;

public class ReportFactoryConsole implements ReportFactory {

    public Report newReport(String fileName) throws JBookTraderException {
        return new Report(new TextReportRenderer(), new PrintWriter(System.out));
    }

}

/* $Id$ */
