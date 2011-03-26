package com.jbooktrader.platform.preferences;

public enum JBTPreferences {
    // TWS connection
    Host("Host", "localhost"),
    Port("Port", "7496"),
    ClientID("Client ID", "0"),

    // Web Access
    WebAccess("Web Access", "enabled"),
    WebAccessPort("Web Access Port", "1235"),
    WebAccessUser("Web Access User", "admin"),
    WebAccessPassword("Web Access Password", "admin"),

    // Data file for backtester and optimizer
    DataFileName("dataFileName"),

    // Date range
    DateRangeStart("dateRange.start", "January 1, 2011"),
    DateRangeEnd("dateRange.end", "March 11, 2011"),
    UseDateRange("dateRange.use", "false"),

    // Optimizer
    OptimizerMinTrades("optimizer.minTrades", "50"),
    OptimizerSelectBy("optimizer.selectBy", "PI"),
    OptimizerMethod("optimizer.method", "Brute force"),
    OptimizerWindowWidth("optimizerwindow.width", "950"),
    OptimizerWindowHeight("optimizerwindow.height", "750"),

    // Main window
    MainWindowWidth("mainwindow.width", "950"),
    MainWindowHeight("mainwindow.height", "400"),

    // Performance chart
    PerformanceChartWidth("performance.chart.width", "950"),
    PerformanceChartHeight("performance.chart.height", "750"),
    PerformanceChartBarSize("performance.chart.barSize", "1 minute"),

    // Optimizer
    DivideAndConquerCoverage("Divide & Conquer coverage", "200"),
    StrategiesPerProcessor("Strategies per processor", "50"),
    InclusionCriteria("Results inclusion criteria", "Profitable strategies"),

    // Optimization Map
    OptimizationMapWidth("optimization.map.width", "720"),
    OptimizationMapHeight("optimization.map.height", "550"),

    // Time Server
    NTPTimeServer("NTP time server", "ntp2.usno.navy.mil");

    private final String name, defaultValue;

    JBTPreferences(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    JBTPreferences(String name) {
        this(name, "");
    }


    public String getDefault() {
        return defaultValue;
    }

    public String getName() {
        return name;
    }
}
