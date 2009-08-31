package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.*;

import java.io.*;
import java.text.*;
import java.util.*;


public final class Report {
    private final static String FILE_SEP = System.getProperty("file.separator");
    private final static String REPORT_DIR = JBookTrader.getAppPath() + FILE_SEP + "reports" + FILE_SEP;
    private final String fieldStart, fieldEnd, rowStart, rowEnd, fieldBreak;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS z");
    private PrintWriter writer;
    private static boolean isDisabled;

    public Report(String fileName) throws JBookTraderException {
        fieldStart = "<td>";
        fieldEnd = "</td>";
        rowStart = "<tr>";
        rowEnd = "</tr>";
        fieldBreak = "<br>";


        if (isDisabled) {
            return;
        }

        File reportDir = new File(REPORT_DIR);
        if (!reportDir.exists()) {
            reportDir.mkdir();
        }

        String fullFileName = REPORT_DIR + fileName + ".htm";
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));
        } catch (IOException ioe) {
            throw new JBookTraderException(ioe);
        }
        StringBuilder s = new StringBuilder();
        String dateAndTime = dateFormat.format(getDate()) + " " + timeFormat.format(getDate());
        s.append("<b>").append("New Report Started: ").append(dateAndTime).append("</b>");
        reportDescription(s.toString());
        s = new StringBuilder();
        s.append("<b>").append("JBT Version: ").append(JBookTrader.VERSION).append("</b>");
        s.append("<table border=1 width=100%>");
        reportDescription(s.toString());
    }

    public static void disable() {
        isDisabled = true;
    }

    public static void enable() {
        isDisabled = false;
    }

    private void report(StringBuilder message) {
        StringBuilder s = new StringBuilder();
        s.append(rowStart);
        s.append(fieldStart).append(dateFormat.format(getDate())).append(fieldEnd);
        s.append(fieldStart).append(timeFormat.format(getDate())).append(fieldEnd);
        s.append(fieldStart).append(message).append(fieldEnd);
        s.append(rowEnd);
        write(s);
    }


    public void report(String message) {
        if (!isDisabled) {
            report(new StringBuilder(message));
        }
    }

    public void report(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        boolean saved = isDisabled;
        isDisabled = false;//always report exceptions
        report(sw.toString());
        isDisabled = saved;
    }

    public void reportDescription(String message) {
        StringBuilder s = new StringBuilder();
        s.append(message).append(fieldBreak);
        write(s);
    }

    public void report(List<?> columns) {
        StringBuilder s = new StringBuilder();
        s.append(rowStart);
        for (Object column : columns) {
            s.append(fieldStart).append(column).append(fieldEnd);
        }
        s.append(rowEnd);
        write(s);
    }

    public void report(List<?> columns, String date, String time) {
        StringBuilder s = new StringBuilder();
        s.append(rowStart);

        s.append(fieldStart);
        s.append(date);
        s.append(fieldEnd);
        s.append(fieldStart);
        s.append(time);
        s.append(fieldEnd);


        for (Object column : columns) {
            s.append(fieldStart).append(column).append(fieldEnd);
        }

        s.append(rowEnd);
        write(s);
    }

    private Date getDate() {
        Dispatcher.Mode mode = Dispatcher.getMode();
        if (mode == Dispatcher.Mode.ForwardTest || mode == Dispatcher.Mode.Trade) {
            return new Date(NTPClock.getInstance().getTime());
        } else {
            return new Date();
        }
    }

    private synchronized void write(StringBuilder s) {
        if (!isDisabled) {
            writer.println(s);
            writer.flush();
        }
    }


}
