package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.*;


public class OptimizationReport extends StrategyReport {

    public OptimizationReport(String reportName) throws JBookTraderException {
        super(reportName);
    }

    public void reportDescription(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(FIELD_BREAK);
        write(sb);
    }

}
