package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.startup.*;

import java.io.*;

public class ReportFactoryFile implements ReportFactory {
    private final static String FILE_SEP = System.getProperty("file.separator");
    private final static String REPORT_DIR = JBookTrader.getAppPath() + FILE_SEP + "reports" + FILE_SEP;

    public Report newReport(String fileName) throws JBookTraderException {
        String reportRendererClass = PreferencesHolder.getInstance().get(JBTPreferences.ReportRenderer);

        ReportRenderer renderer;
        try {
            Class<? extends ReportRenderer> clazz = Class.forName(reportRendererClass).asSubclass(ReportRenderer.class);
            renderer = clazz.newInstance();
        } catch (Exception e) {
            throw new JBookTraderException(e);
        }

        PrintWriter writer = null;
        if (!Report.isDisabled()) {
            File reportDir = new File(REPORT_DIR);
            if (!reportDir.exists()) {
                reportDir.mkdir();
            }

            String fullFileName = REPORT_DIR + fileName + "." + renderer.getFileExtension();
            try {
                writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));
            } catch (IOException ioe) {
                throw new JBookTraderException(ioe);
            }
        }

        return new Report(renderer, writer);
    }

}

/* $Id$ */
