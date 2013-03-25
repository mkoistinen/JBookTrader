package com.jbooktrader.platform.report;

import java.io.*;
import java.util.*;

/**
 * @author Eugene Kononov
 */
public class OptimizationReport extends StrategyReport {

    public OptimizationReport(String reportName) throws IOException {
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
