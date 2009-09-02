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

    private final PrintWriter writer;

    public Report(String fileName) throws JBookTraderException {
        String fileSeparator = System.getProperty("file.separator");
        String reportDirPath = JBookTrader.getAppPath() + fileSeparator + "reports" + fileSeparator;
        File reportDir = new File(reportDirPath);
        if (!reportDir.exists()) {
            reportDir.mkdir();
        }

        String fullFileName = reportDirPath + fileName + ".htm";
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));
        } catch (IOException ioe) {
            throw new JBookTraderException(ioe);
        }
        StringBuilder sb = new StringBuilder();
        //String dateAndTime = dateFormat.format(getDate()) + " " + timeFormat.format(getDate());
        //sb.append("<b>").append("New Report Started: ").append(dateAndTime).append("</b>");
        reportDescription(sb.toString());
        sb = new StringBuilder();
        sb.append("<b>").append("JBT Version: ").append(JBookTrader.VERSION).append("</b>");
        sb.append("<table border=1 width=100%>");
        reportDescription(sb.toString());
    }


    public void reportDescription(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(FIELD_BREAK);
        write(sb);
    }


    protected synchronized void write(StringBuilder sb) {
        writer.println(sb);
        writer.flush();
    }
}
