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

    private static final String WEBROOT = JBookTrader.getAppPath() + "/resources/web";

    public void handle(HttpExchange httpExchange) throws IOException {
        URI uri = httpExchange.getRequestURI();
        String resource = uri.getPath();
        StringBuilder response = new StringBuilder();

        // The index.html page. This is VIRTUAL, it is not on the filesystem.
        if (resource.equals("/index.html")) {
            // We'll respond to any unknown request with the main page
            response.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
            response.append("<html>\n");
            response.append("<head>\n");
            response.append("<title>JBookTrader Web Console</title>\n");

            boolean isIPhone = httpExchange.getRequestHeaders().getFirst("User-Agent").contains("iPhone");
            if (isIPhone) {
                response.append("<link rel=\"apple-touch-icon\" href=\"apple-touch-icon.png\" />\n");
                response.append("<meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;\" />\n");
                response.append("<link media=\"screen\" rel=\"stylesheet\" type=\"text/css\" href=\"iphone.css\" />\n");
                response.append("<script type=\"application/x-javascript\" src=\"iphone.js\"></script> \n");
            } else {
                response.append("<link media=\"screen\" rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\" />\n");
            }

            response.append("</head>\n");
            response.append("<body>\n");
            response.append("<h1>\n");
            response.append(JBookTrader.APP_NAME).append(": ").append(Dispatcher.getMode().getName());
            response.append("</h1>\n");

            Collection<Strategy> strategies = Dispatcher.getTrader().getAssistant().getAllStrategies();
            List<Strategy> strategyList = new ArrayList<Strategy>(strategies);
            Collections.sort(strategyList);

            TableLayout tableLayout = null;
            PreferencesHolder prefs = PreferencesHolder.getInstance();
            String tableLayoutPreference = prefs.get(JBTPreferences.WebAccessTableLayout);
            if (tableLayoutPreference.equals("simple")) {
                tableLayout = new SimpleTableLayout(response, strategyList);
            }

            if (tableLayoutPreference.equals("grouped")) {
                tableLayout = new GroupedTableLayout(response, strategyList);
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
            String absoluteResource = resource.startsWith("/reports/") ? (JBookTrader.getAppPath() + resource) : (WEBROOT + resource);

            FileHandler fileHandler = new FileHandler();
            String fileName = fileHandler.getFileName(uri);
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
