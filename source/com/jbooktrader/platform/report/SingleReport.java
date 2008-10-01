package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;

import java.io.*;
import java.text.*;
import java.util.*;

public final class SingleReport extends AbstractReport {
    private final String fieldStart, fieldEnd, rowStart, rowEnd, fieldBreak;
    private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss MM/dd/yy z");
    private PrintWriter writer;
    public SingleReport(ReportRenderer renderer, PrintWriter writer) throws JBookTraderException {
        fieldStart = renderer.getFieldStart();
        fieldEnd = renderer.getFieldEnd();
        rowStart = renderer.getRowStart();
        rowEnd = renderer.getRowEnd();
        fieldBreak = renderer.getFieldBreak();
        String emphasisStart = renderer.getEmphasisStart();
        String emphasisEnd = renderer.getEmphasisEnd();
        String rootStart = renderer.getRootStart();

        if (Dispatcher.isReportDisabled()) {
            return;
        }

        this.writer = writer;

        StringBuilder s = new StringBuilder();
        s.append(emphasisStart).append("New Report Started: ").append(df.format(getDate())).append(emphasisEnd);
        reportDescription(s.toString());
        s = new StringBuilder();
        s.append(emphasisStart).append(JBookTrader.APP_NAME).append(" Version: ").append(JBookTrader.VERSION).append(emphasisEnd);
        s.append(rootStart);
        reportDescription(s.toString());
    }

    public void report(StringBuilder message) {
        StringBuilder s = new StringBuilder();
        s.append(rowStart);
        s.append(fieldStart).append(df.format(getDate())).append(fieldEnd);
        s.append(fieldStart).append(message).append(fieldEnd);
        s.append(rowEnd);
        write(s);
    }


    public void report(String message) {
        if (!Dispatcher.isReportDisabled()) {
            report(new StringBuilder(message));
        }
    }

    public void report(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        boolean saved = Dispatcher.isReportDisabled();
        Dispatcher.enableReport();//always report exceptions
        report(sw.toString());
        if(saved) {
            Dispatcher.disableReport();
        }
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

    public void report(List<?> columns, String time) {
        StringBuilder s = new StringBuilder();
        s.append(rowStart);

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
        return Calendar.getInstance(TimeZone.getDefault()).getTime();
    }

    private synchronized void write(StringBuilder s) {
        if (!Dispatcher.isReportDisabled()) {
            writer.println(s);
            writer.flush();
        }
    }

}
