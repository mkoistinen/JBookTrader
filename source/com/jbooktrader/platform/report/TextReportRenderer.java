package com.jbooktrader.platform.report;

public class TextReportRenderer implements ReportRenderer {
    private static final String LINE_SEP = System.getProperty("line.separator");

    public String getFieldStart() {
        return "";
    }

    public String getFieldEnd() {
        return "	";
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
        return LINE_SEP;
    }

    public String getRootStart() {
        return "";
    }

    public String getFileExtension() {
        return "txt";
    }
}
