package com.jbooktrader.platform.chart;

/**
 * Bar sizes for performance charts
 *
 * @author Eugene Kononov
 */
public enum BarSize {
    Second5("5 seconds", 5),
    Second15("15 seconds", 15),
    Second30("30 seconds", 30),
    Minute1("1 minute", 60),
    Minute5("5 minutes", 300),
    Minute15("15 minutes", 900),
    Minute30("30 minutes", 1800),
    Hour1("1 hour", 3600);

    private final String name;
    private final int barSize;

    BarSize(String name, int barSize) {
        this.name = name;
        this.barSize = barSize;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return barSize * 1000;
    }


    public static BarSize getBarSize(String name) {
        for (BarSize barSize : values()) {
            if (barSize.name.equals(name)) {
                return barSize;
            }
        }
        return null;
    }
}
