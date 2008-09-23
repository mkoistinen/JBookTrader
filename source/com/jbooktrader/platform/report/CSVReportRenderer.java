package com.jbooktrader.platform.report;

public class CSVReportRenderer implements ReportRenderer {

    public String getFieldStart() {
        return "";
    }

    public String getFieldEnd() {
        return ",";
    }

    public String getRowStart() {
        return "";
    }

    public String getRowEnd() {
        return "";
    }

    public String getEmphasisStart() {
        return "";
    }

    public String getEmphasisEnd() {
        return "";
    }

    public String getFieldBreak() {
        return " ";
    }

    public String getRootStart() {
        return "";
    }

    public String getFileExtension() {
        return "csv";
    }


}
