package com.jbooktrader.platform.web;

import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;

/**
 * Handles HTTP requests for files (images, css, js, etc.)
 */
public class FileHandler {
    public boolean handleFile(HttpExchange httpExchange, String resource, ContentType fileType) throws IOException {

        if (resource == null) return false;

        File file = new File(resource);

        try {
            if (!file.exists()) {
                return false;
            }
        }
        catch (Exception e) {
            /* We don't really care which one */
            return false;
        }

        Headers responseHeaders = httpExchange.getResponseHeaders();

        responseHeaders.add("Pragma", "public");
        responseHeaders.add("Expires", "0");
        responseHeaders.add("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        responseHeaders.add("Cache-Control", "public");
        responseHeaders.add("Content-Type", fileType.getContentType() + ";charset=utf-8");
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
     *
     * @param uri
     * @return String resource
     */
    public String getFileName(URI uri) {
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


}
