package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.*;

import java.util.*;


public class StrategyReport extends Report {

    public StrategyReport(String reportName) throws JBookTraderException {
        super(reportName);
    }

    public void report(List<String> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append(ROW_START);
        for (String column : columns) {
            sb.append(FIELD_START).append(column).append(FIELD_END);
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
