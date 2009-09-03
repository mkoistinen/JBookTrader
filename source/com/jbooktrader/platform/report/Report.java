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
    private boolean openBody = false;
    private boolean newFile = false;

    static {
        String fileSeparator = System.getProperty("file.separator");
        REPORT_DIR = JBookTrader.getAppPath() + fileSeparator + "reports" + fileSeparator;
        File reportDir = new File(REPORT_DIR);
        if (!reportDir.exists()) {
            reportDir.mkdir();
        }
    }
    
    public Report(String fileName, String reportDescription) throws JBookTraderException {
    	Dispatcher.registerReport(this);

        String fullFileName = REPORT_DIR + fileName + ".htm";
        File reportFile = new File(fullFileName);
        if (!reportFile.exists()) newFile = true;
        
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));
        } catch (IOException ioe) {
            throw new JBookTraderException(ioe);
        }
        
        StringBuilder sb = new StringBuilder();

        // Write the whole file preamble since this is a new file
        if (newFile) {
        	sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        	sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n");
        	sb.append("<head>\n");
        	sb.append("\t<title>JBookTrader: ").append(reportDescription).append("</title>\n");
        	sb.append("\t<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />\n");
        	sb.append("\t<link rel=\"stylesheet\" type=\"text/css\" href=\"report.css\" />\n");
        	sb.append("</head>\n");
        }
        
        write(sb);
        sb = new StringBuilder();
        
        // Create the document body -- one for each launch of JBT.
        sb.append("<body>\n");
        sb.append("\t<h1>").append("JBT Version: ").append(JBookTrader.VERSION).append("</h1>\n");
        sb.append("\t<table cellspacing=\"0\" cellpadding=\"0\">\n");
        openBody = true;
        write(sb);
    }

    public void reportDescription(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(FIELD_BREAK).append("\n");
        write(sb);
    }

    protected synchronized void write(StringBuilder sb) {
        writer.println(sb);
        writer.flush();
    }

	/*
	 * We should ensure that this is called when shutting down
	 */
    public void close() {
    	Dispatcher.deregisterReport(this);
    	if (writer != null) {
    		if (openBody) write(new StringBuilder("\t</table>\n</body>\n"));
    		write(new StringBuilder("</html>\n"));
	    	writer.close();
    	}
    }
    
    /*
     * Here we *hope* that GC will help us out (fat chance)
     */
    public void finalize() {
    	close();
    }

}