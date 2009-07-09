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

    public void handle(HttpExchange httpExchange) throws IOException {
        URI uri = httpExchange.getRequestURI();
        String resource = uri.getPath();
        String absoluteResource = "";
        FileHandler fileHandler = new FileHandler();
        String fileName = fileHandler.getFileName(uri);
        ContentType fileType = ContentType.getContentType(fileName);

        boolean isIPhone = httpExchange.getRequestHeaders().getFirst("User-Agent").contains("iPhone");

        StringBuilder response = new StringBuilder();

        // We support a VIRTUAL directory '/reports/' which we manually map onto the reports folder in the class path
        // This must explicitly be the beginning of the requested resource.  Otherwise, we fold any requests over to
        // the WEBROOT
        if (resource.startsWith("/reports/")) {
            absoluteResource = resource.replaceFirst("/reports", REPORTROOT);
        } else {
            absoluteResource = WEBROOT + resource;
        }

        // First, redirect for default page
        if (resource.equals("") || resource.equals("/")) {
            httpExchange.getResponseHeaders().add("Location", "/index.html");
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
            int totalTrades = 0;

            for (Strategy strategy : Dispatcher.getTrader().getAssistant().getAllStrategies()) {
                PositionManager positionManager = strategy.getPositionManager();
                PerformanceManager performanceManager = strategy.getPerformanceManager();
                totalNetProfit += performanceManager.getNetProfit();
                totalTrades += performanceManager.getTrades();

                response.append("<tr>\n");
                response.append("<td><a href=\"/reports/").append(strategy.getName()).append(".htm\">").append(strategy.getName()).append("</a></td>");
                response.append("<td align=\"right\">").append(positionManager.getPosition()).append("</td>");
                response.append("<td align=\"right\">").append(performanceManager.getTrades()).append("</td>");
                response.append("<td align=\"right\">").append(df.format(performanceManager.getMaxDrawdown())).append("</td>");
                response.append("<td align=\"right\">").append(df.format(performanceManager.getNetProfit())).append("</td>\n");
                response.append("</tr>\n");
            }

            response.append("<tr><td class=\"summary\" colspan=\"2\">Summary</td>");
            response.append("<td class=\"summary\" colspan=\"1\" style=\"text-align: right\">").append(totalTrades).append("</td><td class=\"summary\"><!-- skip this colum --></td>");
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

        else if (fileType != ContentType.UNKNOWN) {
            if (!fileHandler.handleFile(httpExchange, absoluteResource, fileType)) {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length());
                response = new StringBuilder("<h1>404 Not Found</h1>No context found for request");
            }
        } else {
            // Huh?
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length());
            response = new StringBuilder("<h1>404 Not Found</h1>No context found for request");
        }

        OutputStream os = httpExchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }
}
