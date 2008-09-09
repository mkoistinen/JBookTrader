package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.ReportRenderer;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.startup.*;

import java.io.*;
import java.text.*;
import java.util.*;


public final class Report {
    private final static String FILE_SEP = System.getProperty("file.separator");
    private final static String REPORT_DIR = JBookTrader.getAppPath() + FILE_SEP + "reports" + FILE_SEP;
    private final String fieldStart, fieldEnd, rowStart, rowEnd, fieldBreak;
    private final ReportRenderer renderer;
    private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss MM/dd/yy z");
    private PrintWriter writer;
    private static boolean isDisabled;

    public Report(String fileName) throws JBookTraderException {
        String reportRendererClass = PreferencesHolder.getInstance().get(ReportRenderer);

        try {
            Class<? extends ReportRenderer> clazz = Class.forName(reportRendererClass).asSubclass(ReportRenderer.class);
            renderer = clazz.newInstance();
        } catch (Exception e) {
            throw new JBookTraderException(e);
        }

        fieldStart = renderer.getFieldStart();
        fieldEnd = renderer.getFieldEnd();
        rowStart = renderer.getRowStart();
        rowEnd = renderer.getRowEnd();
        fieldBreak = renderer.getFieldBreak();
        String emphasisStart = renderer.getEmphasisStart();
        String emphasisEnd = renderer.getEmphasisEnd();
        String rootStart = renderer.getRootStart();
        String fileExtension = renderer.getFileExtension();


        if (isDisabled) {
            return;
        }

        File reportDir = new File(REPORT_DIR);
        if (!reportDir.exists()) {
            reportDir.mkdir();
        }

        String fullFileName = REPORT_DIR + fileName + "." + fileExtension;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));
        } catch (IOException ioe) {
            throw new JBookTraderException(ioe);
        }
        StringBuilder s = new StringBuilder();
        s.append(emphasisStart).append("New Report Started: ").append(df.format(getDate())).append(emphasisEnd);
        reportDescription(s.toString());
        s = new StringBuilder();
        s.append(emphasisStart).append("JBT Version: ").append(JBookTrader.VERSION).append(emphasisEnd);
        s.append(rootStart);
        reportDescription(s.toString());
    }

    public ReportRenderer getRenderer() {
        return renderer;
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
        s.append(fieldStart).append(df.format(getDate())).append(fieldEnd);
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
        if (!isDisabled) {
            writer.println(s);
            writer.flush();
        }
    }


}
