package com.jbooktrader.platform.web;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.text.*;

public class WebHandler implements HttpHandler {

    private static final String WEBROOT = JBookTrader.getAppPath() + "/resources/web";
    private static final String REPORTROOT = JBookTrader.getAppPath() + "/reports";

    /**
     * If you add any types here, be sure that you also support them in the getType() method
     * @author mkoistinen
     */
    public enum Type {
		HTML("html", "text/html"), HTM("htm", "text/html"), 
		CSS("css", "text/css"), JS("js", "text/javascript"),
		PNG("png", "image/png"), JPG("jpg", "image/jpeg"), 
		JPEG("jpeg", "image/jpeg"), GIF("gif", "image/gif"), 
		ICO("ico", "image/x-ico"), 
		UNKNOWN("unknown", "application/octent-stream");
    	
		private String extension, contentType;
    	
    	private Type(String extension, String contentType) {
    		this.extension = extension;
    		this.contentType = contentType;
    	}
    	
    	public String getExtension() {
    		return extension;
    	}
    	
    	public String getContentType() {
    		return contentType;
    	}
    	
    	public String toString() {
    		return "Type: " + getExtension() + ", ContentType: " + getContentType();
    	}
    	
    }
    
    public void handle(HttpExchange httpExchange) throws IOException {        
        URI uri = httpExchange.getRequestURI();
        String resource = uri.getPath();
        String absoluteResource = "";
        String file = getFileName(uri);
        Type fileType = getType(file);
        
        boolean isIPhone = httpExchange.getRequestHeaders().getFirst("User-Agent").contains("iPhone");
        
        StringBuilder response = new StringBuilder();
                
        // We support a VIRTUAL directory '/reports/' which we manually map onto the reports folder in the class path
        // This must explicitly be the beginning of the requested resource.  Otherwise, we fold any requests over to
        // the WEBROOT
        if (resource.startsWith("/reports/")) {
        	absoluteResource = resource.replaceFirst("/reports", REPORTROOT);
        }
        else {
        	absoluteResource = WEBROOT + resource;
        }
        
        // First, redirect for default page
        if (resource == null || resource.equals("") || resource.equals("/")) {
        	response.append("File not found");
        	httpExchange.getResponseHeaders().set("Location", "/index.html");
        	httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, response.length());
        }
        
        // The index.html page...
        // This is VIRTUAL, it is not on the filesystem.
        else if (resource.equals("/index.html")) {

            // We'll respond to any unknown request with the main page
            response.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
            response.append("<html>\n");
            response.append("<head>\n");
            response.append("<title>JBookTrader Web Console</title>\n");

            if (isIPhone) {
                response.append("<link rel=\"apple-touch-icon\" href=\"apple-touch-icon.png\" />\n");
                response.append("<meta name=\"viewport\" content=\"width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;\" />\n");
                response.append("<link media=\"screen\" rel=\"stylesheet\" type=\"text/css\" href=\"iPhone.css\" />\n");
                response.append("<script type=\"application/x-javascript\" src=\"iPhone.js\"></script> \n");
            } else {
                response.append("<link media=\"screen\" rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\" />\n");
            }

            response.append("</head>\n");
            response.append("<body>\n");
            response.append("<h1>\n");
            response.append(JBookTrader.APP_NAME).append(": ").append(Dispatcher.getMode().getPresentParticiple());
            response.append("</h1>\n");

            response.append("<table>");
            response.append("<tr><th>Strategy</th><th>Position</th><th>Trades</th><th>Max DD</th><th>Net Profit</th></tr>");
            DecimalFormat df = NumberFormatterFactory.getNumberFormatter(0);

            double totalNetProfit = 0.0;

            for (Strategy strategy : Dispatcher.getTrader().getAssistant().getAllStrategies()) {
                PositionManager positionManager = strategy.getPositionManager();
                PerformanceManager performanceManager = strategy.getPerformanceManager();
                totalNetProfit += performanceManager.getNetProfit();

                response.append("<tr>\n");
                response.append("<td><a href=\"/reports/").append(strategy.getName()).append(".htm\">").append(strategy.getName()).append("</a></td>");
                response.append("<td align=\"right\">").append(positionManager.getPosition()).append("</td>");
                response.append("<td align=\"right\">").append(performanceManager.getTrades()).append("</td>");
                response.append("<td align=\"right\">").append(df.format(performanceManager.getMaxDrawdown())).append("</td>");
                response.append("<td align=\"right\">").append(df.format(performanceManager.getNetProfit())).append("</td>\n");
                response.append("</tr>\n");
            }

            response.append("<tr><td class=\"summary\" colspan=\"4\">Summary</td>");
            response.append("<td class=\"summary\" style=\"text-align: right\">").append(df.format(totalNetProfit)).append("</td>\n");

            response.append("</table>\n");
            response.append("<p class=\"version\">JBookTrader version ").append(JBookTrader.VERSION).append("</p>\n");
            response.append("<p class=\"eventReport\"><a href=\"/reports/EventReport.htm\">Event Report</a></p>\n");
            response.append("<p>&nbsp;</p>");
            response.append("</body>\n");
            response.append("</html>\n");
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        } 
        
        // ALL dynamic pages must be in the if/then/else sequence above this point
        // Static resources from here down
        
        else if (fileType != Type.UNKNOWN) {
            if (!handleFile(httpExchange, absoluteResource, fileType)) {
            	response.append("File not found");
            	httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length());
            }
        } else {
            // Huh?
            response.append("File not found");
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length());
        }

        OutputStream os = httpExchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }

	/**
     * Handles HTTP requests for files (images, css, js, etc.)
     */
    private boolean handleFile(HttpExchange httpExchange, String resource, Type fileType) throws IOException {
        File file = new File(resource);
        if (!file.exists()) { return false; }
        
        Headers responseHeaders = httpExchange.getResponseHeaders();
        
        responseHeaders.set("Content-Type", fileType.getContentType() + ";charset=utf-8");
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, file.length());
        
        OutputStream responseBody = httpExchange.getResponseBody();
        try {
            FileInputStream fileStream = new FileInputStream(resource);
            BufferedInputStream bis = new BufferedInputStream(fileStream);

            byte buffer[] = new byte[8192];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                responseBody.write(buffer, 0, bytesRead);
            }
            bis.close();
        }
        finally {
            responseBody.flush();
            responseBody.close();
        }
        
        return true;
    }
        
    /**
     * Returns just the resource-portion of the URI
     * @param uri
     * @return String resource
     */
    private String getFileName(URI uri) {
    	String url = uri.toString();
    	
    	int start = url.lastIndexOf("/") + 1;
    	int end = url.length();
    	
    	int tmp = url.indexOf("?", start);
    	if (tmp > start && tmp < end) end = tmp;
    	
    	tmp = url.indexOf("#", start);
    	if (tmp > start && tmp < end) end = tmp;
    	
    	if (start > -1 && start < url.length() - 1) {
    		return url.substring(start, end);
    	}
    	
    	return "";
    }
        
    /**
     * Locates the final extension ".xxx" of the resource name and returns a Type
     * @param resource
     * @return Type for the file extension
     */
    private Type getType(String resource) {
    	try {
    		int dot = resource.lastIndexOf(".");
	    	if (dot > -1 && dot < resource.length()-1) {
	    		String ext = resource.substring(dot + 1).toLowerCase();
	    		if (ext.equals(Type.HTML.getExtension())) return Type.HTML;
	    		else if (ext.equals(Type.HTM.getExtension())) return Type.HTM;
	    		else if (ext.equals(Type.CSS.getExtension())) return Type.CSS;
	    		else if (ext.equals(Type.JS.getExtension())) return Type.JS;
	    		else if (ext.equals(Type.PNG.getExtension())) return Type.PNG;
	    		else if (ext.equals(Type.JPG.getExtension())) return Type.JPG;
	    		else if (ext.equals(Type.JPEG.getExtension())) return Type.JPEG;
	    		else if (ext.equals(Type.GIF.getExtension())) return Type.GIF;
	    		else if (ext.equals(Type.ICO.getExtension())) return Type.ICO;
	    	}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}

    	return Type.UNKNOWN;
    }
    
}
