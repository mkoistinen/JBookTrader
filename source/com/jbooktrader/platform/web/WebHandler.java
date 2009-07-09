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

    public void handle(HttpExchange httpExchange) throws IOException {
        String requestURI = httpExchange.getRequestURI().toString().trim();
        String userAgent = httpExchange.getRequestHeaders().getFirst("User-Agent");
        boolean iPhone = userAgent.contains("iPhone");

        StringBuilder response = new StringBuilder();
        String fileType = requestURI.toLowerCase();
        boolean isSupportedType = fileType.contains(".png") || fileType.contains(".jpg") || fileType.contains(".gif");
        isSupportedType = isSupportedType || fileType.contains(".ico") || fileType.contains(".css") || fileType.contains(".js");

        // The page...
        if (requestURI.equalsIgnoreCase("/") || requestURI.equalsIgnoreCase("/index.html")) {

            // We'll respond to any unknown request with the main page
            response.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
            response.append("<html>\n");
            response.append("<head>\n");
            response.append("<title>JBookTrader Web Console</title>\n");

            if (iPhone) {
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
            response.append(JBookTrader.APP_NAME).append(": ").append(Dispatcher.getMode().getName());
            response.append("</h1>\n");

            response.append("<table>");
            response.append("<tr><th>Strategy</th><th>Position</th><th>Trades</th><th>Max DD</th><th>Net Profit</th></tr>");
            DecimalFormat df = NumberFormatterFactory.getNumberFormatter(0);

            double totalPNL = 0.0;

            for (Strategy strategy : Dispatcher.getTrader().getAssistant().getAllStrategies()) {
                PositionManager positionManager = strategy.getPositionManager();
                PerformanceManager performanceManager = strategy.getPerformanceManager();
                totalPNL += performanceManager.getNetProfit();

                response.append("<tr>\n");
                response.append("<td>").append(strategy.getName()).append("</td>");
                response.append("<td align=\"right\">").append(positionManager.getPosition()).append("</td>");
                response.append("<td align=\"right\">").append(performanceManager.getTrades()).append("</td>");
                response.append("<td align=\"right\">").append(df.format(performanceManager.getMaxDrawdown())).append("</td>");
                response.append("<td align=\"right\">").append(df.format(performanceManager.getNetProfit())).append("</td>\n");
                response.append("</tr>\n");
            }

            response.append("<tr><td class=\"summary\" colspan=\"4\">Summary</td>");
            response.append("<td class=\"summary\" style=\"text-align: right\">").append(df.format(totalPNL)).append("</td>\n");

            response.append("</table>\n");
            response.append("<p class=\"version\">JBookTrader version ").append(JBookTrader.VERSION).append("</p>\n");
            response.append("</body>\n");
            response.append("</html>\n");
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        } else if (isSupportedType) {
            handleFile(httpExchange, requestURI);
            return;
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
     * The files must reside in resources/web/
     *
     * @param httpExchange
     * @param requestURI
     * @throws IOException
     */
    private void handleFile(HttpExchange httpExchange, String requestURI) throws IOException {

        StringBuilder resource = new StringBuilder(WEBROOT).append(requestURI);

        String requestURILower = requestURI.toLowerCase();
        Headers responseHeaders = httpExchange.getResponseHeaders();
        String contentType = "application/octet-stream";

        if (requestURILower.contains(".png")) {
            contentType = "image/png";
        } else if (requestURILower.contains(".ico")) {
            contentType = "image/x-ico";
        } else if (requestURILower.contains(".jpg")) {
            contentType = "image/jpeg";
        } else if (requestURILower.contains(".gif")) {
            contentType = "image/gif";
        } else if (requestURILower.contains(".css")) {
            contentType = "text/css";
        } else if (requestURILower.contains(".js")) {
            contentType = "text/javascript";
        }

        responseHeaders.set("Content-Type", contentType + ";charset=utf-8");

        long fileLength = 0;

        try {
            File temp = new File(resource.toString());
            fileLength = temp.length();
        }
        catch (Exception e) {
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

    }
}
