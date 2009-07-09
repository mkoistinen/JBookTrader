package com.jbooktrader.platform.web;

/**
 * If you add any types here, be sure that you also support them in the getType() method
 *
 * @author mkoistinen
 */
public enum ContentType {
    HTML("html", "text/html"), HTM("htm", "text/html"),
    CSS("css", "text/css"), JS("js", "text/javascript"),
    PNG("png", "image/png"), JPG("jpg", "image/jpeg"),
    JPEG("jpeg", "image/jpeg"), GIF("gif", "image/gif"),
    ICO("ico", "image/x-ico"),
    UNKNOWN("unknown", "application/octent-stream");

    private String extension, contentType;

    private ContentType(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public String getContentType() {
        return contentType;
    }


    /**
     * Locates the final extension ".xxx" of the resource name and returns a ContentType
     *
     * @param resource
     * @return Type for the file extension
     */
    public static ContentType getContentType(String resource) {
        ContentType contentType = UNKNOWN;
        int dotIndex = resource.lastIndexOf(".");
        if (dotIndex > -1 && dotIndex < resource.length() - 1) {
            String ext = resource.substring(dotIndex + 1).toLowerCase();

            for (ContentType type : values()) {
                if (ext.equals(type.extension)) {
                    contentType = type;
                    break;
                }
            }
        }

        return contentType;
    }


    public String toString() {
        return "Type: " + getExtension() + ", ContentType: " + getContentType();
    }

}
