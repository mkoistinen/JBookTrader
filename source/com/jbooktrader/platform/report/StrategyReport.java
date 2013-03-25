package com.jbooktrader.platform.report;

import java.io.*;
import java.util.*;

/**
 * @author Eugene Kononov
 */
public class StrategyReport extends Report {

    public StrategyReport(String reportName) throws IOException {
        super(reportName);
    }

    public void reportHeaders(List<String> headers) {
        StringBuilder sb = new StringBuilder();
        sb.append(ROW_START);
        for (String column : headers) {
            sb.append(HEADER_START).append(column).append(HEADER_END);
        }
        sb.append(ROW_END);

        write(sb);
    }

    public void report(List<String> columns, String date, String time) {
        StringBuilder sb = new StringBuilder();

        sb.append(ROW_START);
        sb.append(FIELD_START).append(date).append(FIELD_END);
        sb.append(FIELD_START).append(time).append(FIELD_END);
        for (String column : columns) {
            sb.append(FIELD_START).append(column).append(FIELD_END);
        }
        sb.append(ROW_END);

        write(sb);
    }
}
