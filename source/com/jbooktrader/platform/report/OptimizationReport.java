package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.*;

import java.util.*;


public class OptimizationReport extends Report {

    public OptimizationReport(String reportName) throws JBookTraderException {
        super(reportName);
    }

    public void report(List<String> headers) {
        StringBuilder sb = new StringBuilder();
        sb.append(ROW_START);
        for (String column : headers) {
            sb.append(FIELD_START).append(column).append(FIELD_END);
        }
        sb.append(ROW_END);
        write(sb);
    }


    public void reportDescription(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(FIELD_BREAK);
        write(sb);
    }

}
