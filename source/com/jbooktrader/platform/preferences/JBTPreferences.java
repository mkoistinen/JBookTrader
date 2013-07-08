package com.jbooktrader.platform.preferences;

/**
 * @author Eugene Kononov
 */
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

    // Portfolio Manager
    PositionSizePerStrategy("Position size per strategy", "1"),
    MaxOpenPositions("Max open positions", "4"),

    // Forced Exit
    MaxDisconnectionPeriod("Close open positions and stop trading if disconnected from TWS for more than", "120"),

    // Notifications
    SmtpHost("SMTP Host", "smtp.gmail.com"),
    SmtpPort("SMTP Port", "465"),
    SmtpUser("User", "you@gmail.com"),
    SmtpPassword("Password", "password"),
    Subject("Subject", "JBookTrader Notifications"),
    Recipients("Recipients", "you@gmail.com, somebodyelse@gmail.com"),
    SendTestNotification("Test", "Send a test notification"),

    // Sub-account
    SubAccount("FA sub-account", ""),

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
    NTPTimeServer("NTP time server", "pool.ntp.org");

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
