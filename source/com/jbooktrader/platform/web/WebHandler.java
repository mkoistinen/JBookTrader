package com.jbooktrader.platform.web;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class WebHandler implements HttpHandler {

    public static final String WEBROOT = JBookTrader.getAppPath() + "/resources";
    public static final String REPORT_DIR = "/reports/";

    public void handle(HttpExchange httpExchange) throws IOException {
        Dispatcher dispatcher = Dispatcher.getInstance();
        URI requestURI = httpExchange.getRequestURI();
        String resource = requestURI.getPath();
        StringBuilder response = new StringBuilder();


        if (resource.equals("") || resource.equals("/")) {
            response.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
            response.append("<html>\n<head>\n<title>\n");
            response.append(JBookTrader.APP_NAME).append(", version: ").append(JBookTrader.VERSION).append("</title>\n");
            response.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"JBookTrader.css\" />\n");
            response.append("</head>\n");

            response.append("<body>\n");
            response.append("<h1>");
            response.append(JBookTrader.APP_NAME).append(" - ");
            response.append("<a href=\"/reports/EventReport.htm\" target=\"_new\">" + dispatcher.getMode().getName() + "</a>");
            response.append("</h1>");

            List<Strategy> strategies = new ArrayList<Strategy>(dispatcher.getTrader().getAssistant().getAllStrategies());
            Collections.sort(strategies);


            SimpleTableLayout tableLayout = new SimpleTableLayout(response, strategies);
            tableLayout.render();

            response.append("</body>\n");
            response.append("</html>\n");
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        } else {
            String absoluteResource = resource.startsWith(REPORT_DIR) ? (JBookTrader.getAppPath() + resource) : (WEBROOT + resource);
            int resourceLength = (int) new File(absoluteResource).length();
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, resourceLength);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(absoluteResource));
            byte[] buffer = new byte[resourceLength];
            bis.read(buffer);
            httpExchange.getResponseBody().write(buffer, 0, resourceLength);
            bis.close();
        }

        OutputStream os = httpExchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }

}
