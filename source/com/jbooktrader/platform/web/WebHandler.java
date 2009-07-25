package com.jbooktrader.platform.web;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class WebHandler implements HttpHandler {

    public static final String WEBROOT = JBookTrader.getAppPath() + "/resources/web";
    public static final String REPORT_DIR = "/reports/";

    public void handle(HttpExchange httpExchange) throws IOException {
        URI requestURI = httpExchange.getRequestURI();
        String resource = requestURI.getPath();
        StringBuilder response = new StringBuilder();

        String userAgent = httpExchange.getRequestHeaders().getFirst("User-Agent");

        boolean isIPhone = userAgent.contains("iPhone");

        if (resource.equals("/update.html")) {
            List<Strategy> strategies = new ArrayList<Strategy>(Dispatcher.getTrader().getAssistant().getAllStrategies());
            Collections.sort(strategies);

            TableLayout tableLayout = new AJAXResponse(response, strategies);
            tableLayout.render();

            httpExchange.getResponseHeaders().add("Expires", "-1");
            httpExchange.getResponseHeaders().add("Cache-Control", "no-cache");
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        } else if (resource.equals("/index.html")) {
            // The index.html page. This is VIRTUAL, it is not on the filesystem.
            // We'll respond to any unknown request with the main page
            response.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
            response.append("<html>\n");
            response.append("<head>\n");
            response.append("<title>JBookTrader Web Console</title>\n");

            if (isIPhone) {
                response.append("<link rel=\"apple-touch-icon\" href=\"apple-touch-icon.png\" />\n");
                response.append("<meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;\" />\n");
                response.append("<link media=\"screen\" rel=\"stylesheet\" type=\"text/css\" href=\"iphone.css\" />\n");
                response.append("<script type=\"text/javascript\" src=\"iphone.js\"></script> \n");
            } else {
                response.append("<link media=\"screen\" rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\" />\n");
            }

            response.append("<script type=\"text/javascript\" src=\"ajax.js\"></script> \n");

            response.append("</head>\n");
            response.append("<body>\n");
            response.append("<h1>\n");
            response.append(JBookTrader.APP_NAME).append(": ").append(Dispatcher.getMode().getName());
            response.append("</h1>\n");

            List<Strategy> strategies = new ArrayList<Strategy>(Dispatcher.getTrader().getAssistant().getAllStrategies());
            Collections.sort(strategies);

            TableLayout tableLayout = null;
            PreferencesHolder prefs = PreferencesHolder.getInstance();
            String tableLayoutPreference = prefs.get(JBTPreferences.WebAccessTableLayout);
            if (tableLayoutPreference.equals("simple")) {
                tableLayout = new SimpleTableLayout(response, strategies);
            }

            if (tableLayoutPreference.equals("grouped")) {
                tableLayout = new GroupedTableLayout(response, strategies);
            }

            if (tableLayout != null) {
                tableLayout.render();
            }

            response.append("<p class=\"version\">JBookTrader version ").append(JBookTrader.VERSION).append("</p>\n");
            response.append("<p class=\"eventReport\"><a href=\"/reports/EventReport.htm\" target=\"_new\">Event Report</a></p>\n");
            response.append("</body>\n");
            response.append("</html>\n");
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        } else if (resource.equals("") || resource.equals("/")) {
            // Redirect for default page
            httpExchange.getResponseHeaders().add("Location", "/index.html");
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, response.length());
        } else {
            // We support a VIRTUAL directory '/reports/' which we manually map onto the reports folder in the class path
            // This must explicitly be the beginning of the requested resource.  Otherwise, we fold any requests over to
            // the WEBROOT
            String absoluteResource = resource.startsWith(REPORT_DIR) ? (JBookTrader.getAppPath() + resource) : (WEBROOT + resource);

            FileHandler fileHandler = new FileHandler();
            String fileName = fileHandler.getFileName(requestURI);
            ContentType fileType = ContentType.getContentType(fileName);
            if (!fileHandler.handleFile(httpExchange, absoluteResource, fileType)) {
                response = new StringBuilder("<h1>404 Not Found</h1>No context found for request");
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length());
            }
        }

        OutputStream os = httpExchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }

}
