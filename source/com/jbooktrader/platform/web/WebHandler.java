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
	
	private static final String WEBROOT = "resources/web";

    public void handle(HttpExchange httpExchange) throws IOException {
    	String requestURI = httpExchange.getRequestURI().toString().trim();
    	String userAgent = httpExchange.getRequestHeaders().getFirst("User-Agent");
    	Boolean iPhone = userAgent.contains("iPhone");

    	StringBuilder sb = new StringBuilder();
    	
    	// The page...
    	if (requestURI.equalsIgnoreCase("/") || requestURI.equalsIgnoreCase("/index.html")) {
    		
    		// We'll respond to any unknown request with the main page
	        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
	        sb.append("<html>\n");
	        sb.append("<head>\n");
	        sb.append("<title>JBookTrader Web Console</title>\n");
	        
	        if (iPhone) {
	        	sb.append("<link rel=\"apple-touch-icon\" href=\"apple-touch-icon.png\" />\n");
	        	sb.append("<meta name=\"viewport\" content=\"width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;\" />\n");
	        	sb.append("<link media=\"screen\" rel=\"stylesheet\" type=\"text/css\" href=\"iPhone.css\" />\n");
	        	sb.append("<script type=\"application/x-javascript\" src=\"iPhone.js\"></script> \n");
	        }
	        else {
	        	sb.append("<link media=\"screen\" rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\" />\n");
	        }

	        String modeString = "";
	        if (Dispatcher.getMode().toString() == "Trade") { modeString = "Trading"; }
	        else if (Dispatcher.getMode().toString() == "Optimization") { modeString = "Optimizing"; }
	        else modeString = Dispatcher.getMode() + "ing";
	        
	        sb.append("</head>\n");
	        sb.append("<body>\n");
	        sb.append("<h1>\n");
	        sb.append(JBookTrader.APP_NAME).append(" : ").append(modeString);
	        sb.append("</h1>\n");
	
	        sb.append("<table>");
	        sb.append("<tr><th>Strategy</th><th>Position</th><th>Trades</th><th>Max DD</th><th>Net Profit</th></tr>");
	        DecimalFormat df = NumberFormatterFactory.getNumberFormatter(0);
	
	        double totalPNL = 0.0;
	        
	        for (Strategy strategy : Dispatcher.getTrader().getAssistant().getAllStrategies()) {
	            PositionManager positionManager = strategy.getPositionManager();
	            PerformanceManager performanceManager = strategy.getPerformanceManager();
	            totalPNL += performanceManager.getNetProfit();
	            
	            sb.append("<tr>\n");
	            sb.append("<td>").append(strategy.getName()).append("</td>");
	            sb.append("<td align=\"right\">").append(positionManager.getPosition()).append("</td>");
	            sb.append("<td align=\"right\">").append(performanceManager.getTrades()).append("</td>");
	            sb.append("<td align=\"right\">").append(df.format(performanceManager.getMaxDrawdown())).append("</td>");
	            sb.append("<td align=\"right\">").append(df.format(performanceManager.getNetProfit())).append("</td>\n");
	            sb.append("</tr>\n");
	        }
	
	        sb.append("<tr><td class=\"summary\" colspan=\"4\">Summary</td>");
	        sb.append("<td class=\"summary\" style=\"text-align: right\">").append(df.format(totalPNL)).append("</td>\n");
	        
	        sb.append("</table>\n");
	        sb.append("<p class=\"version\">JBookTrader version ").append(JBookTrader.VERSION).append("</p>\n");
	        sb.append("</body>\n");
	        sb.append("</html>\n");
    	}
    	
    	// This handles static files...
    	else if (
    		requestURI.toLowerCase().contains(".png") || 
    		requestURI.toLowerCase().contains(".jpg") || 
    		requestURI.toLowerCase().contains(".gif") || 
    		requestURI.toLowerCase().contains(".ico") ||
    		requestURI.toLowerCase().contains(".css") ||
    		requestURI.toLowerCase().contains(".js")) {
    		try {
    			handleFile(httpExchange, requestURI); 
    		}
    		catch (Exception e) {
    			e.printStackTrace(); 
    		}
    		
    		return;
    	}
    	
    	// Huh?
    	else {
    		sb.append("File not found");

    		String response = sb.toString();
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

            return;
    	}
    	
        String response = sb.toString();
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * Handles HTTP requests for files (images, css, js, etc.)
     * The files must reside in resources/web/
     * 
     * @param httpExchange
     * @param requestURI
     * @throws IOException
     */
	private void handleFile(HttpExchange httpExchange, String requestURI) throws IOException {

	    StringBuilder resource = new StringBuilder(WEBROOT).append(requestURI);
	        		
	    if (requestURI.toLowerCase().contains(".png")) {
	    	httpExchange.getResponseHeaders().set("Content-Type", "image/png;charset=utf-8");
	    }
	    else if (requestURI.toLowerCase().contains(".ico")) {
	    	httpExchange.getResponseHeaders().set("Content-Type", "image/x-ico;charset=utf-8");
	    }
	    else if (requestURI.toLowerCase().contains(".jpg")) {
	    	httpExchange.getResponseHeaders().set("Content-Type", "image/jpeg;charset=utf-8");
	    }
	    else if (requestURI.toLowerCase().contains(".gif")) {
	    	httpExchange.getResponseHeaders().set("Content-Type", "image/gif;charset=utf-8");
	    }
	    else if (requestURI.toLowerCase().contains(".css")) {
	    	httpExchange.getResponseHeaders().set("Content-Type", "text/css;charset=utf-8");
	    }
	    else if (requestURI.toLowerCase().contains(".js")) {
	    	httpExchange.getResponseHeaders().set("Content-Type", "text/javascript;charset=utf-8");
	    }
	    else {
	    	httpExchange.getResponseHeaders().set("Content-Type", "application/octet-stream;charset=utf-8");
	    }
	    
	    long fileLength = 0;

	    try {
	    	File temp = new File(resource.toString());
	    	fileLength = temp.length();
	    }
	    catch(Exception e) {
	    	System.out.println(e);
	    }
	    
	    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, fileLength);

	    OutputStream responseBody = httpExchange.getResponseBody();
	    try {
	    	FileInputStream file = new FileInputStream(resource.toString());
	    	BufferedInputStream bis = new BufferedInputStream(file);

	    	byte buffer[] = new byte[8192];
	    	int bytesRead;
	    	while ((bytesRead = bis.read(buffer)) != -1)
	    		responseBody.write(buffer, 0, bytesRead);
	    	bis.close();
	    }
	    catch (Exception e) {
	    	System.out.println(e);
	    }
	    finally {
	    	responseBody.flush();
	    	responseBody.close();
	    }
	    
	    return;
    }
}
