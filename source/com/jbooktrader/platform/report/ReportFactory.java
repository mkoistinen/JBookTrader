package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.JBookTraderException;

public interface ReportFactory {

    Report newReport(String fileName) throws JBookTraderException;
}

/* $Id$ */
