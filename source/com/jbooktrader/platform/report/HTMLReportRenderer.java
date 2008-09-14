package com.jbooktrader.platform.report;

public class HTMLReportRenderer implements ReportRenderer {

    public String getFieldStart() {
        return "<td>";
    }

    public String getFieldEnd() {
        return "</td>";
    }

    public String getRowStart() {
        return "<tr>";
    }

    public String getRowEnd() {
        return "</tr>";
    }

    public String getEmphasisStart() {
        return "<b>";
    }

    public String getEmphasisEnd() {
        return "</b>";
    }

    public String getFieldBreak() {
        return "<br>";
    }

    public String getRootStart() {
        return "<table border=1 width=100%>";
    }

    public String getFileExtension() {
        return "htm";
    }

}

/* $Id$ */
