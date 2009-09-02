package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;

import java.io.*;


public abstract class Report {
    protected final static String FIELD_START = "<td>";
    protected final static String FIELD_END = "</td>";
    protected final static String ROW_START = "<tr>";
    protected final static String ROW_END = "</tr>";
    protected final static String FIELD_BREAK = "<br>";

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

    public Report(String reportName) throws JBookTraderException {
        String fullFileName = REPORT_DIR + reportName + ".htm";
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));
        } catch (IOException ioe) {
            throw new JBookTraderException(ioe);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("</table><br>"); // close the previously created table, if any
        sb.append("<table border=1 width=100%>");
        write(sb);
    }


    protected synchronized void write(StringBuilder sb) {
        writer.println(sb);
        writer.flush();
    }
}
