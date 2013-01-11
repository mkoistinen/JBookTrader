package com.jbooktrader.platform.report;

import com.jbooktrader.platform.startup.*;

import java.io.*;


public abstract class Report {
    protected static final String FIELD_START = "<td>";
    protected static final String FIELD_END = "</td>";
    protected static final String HEADER_START = "<th>";
    protected static final String HEADER_END = "</th>";
    protected static final String ROW_START = "<tr>";
    protected static final String ROW_END = "</tr>";
    protected static final String FIELD_BREAK = "<br>";

    private static final String REPORT_DIR;
    private final PrintWriter writer;

    static {
        String fileSeparator = System.getProperty("file.separator");
        REPORT_DIR = JBookTrader.getAppPath() + fileSeparator + "reports" + fileSeparator;
        File reportDir = new File(REPORT_DIR);
        if (!reportDir.exists()) {
            reportDir.mkdir();
        }
    }

    protected Report(String reportName) throws IOException {
        String fullFileName = REPORT_DIR + reportName + ".htm";
        File reportFile = new File(fullFileName);
        boolean reportExisted = reportFile.exists();

        writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));
        StringBuilder sb = new StringBuilder();
        if (reportExisted) {
            sb.append("</table><br>"); // close the previously created table
        } else {
            sb.append("<html>");

        }

        sb.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\" width=100%>");
        write(sb);

    }


    protected synchronized void write(StringBuilder sb) {
        writer.println(sb);
        writer.flush();
    }
}
