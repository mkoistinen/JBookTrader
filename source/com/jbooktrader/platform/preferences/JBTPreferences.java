package com.jbooktrader.platform.preferences;

public enum JBTPreferences {
    // TWS connection
    Host("Host", "localhost"),
    Port("Port", "7496"),
    ClientID("Client ID", "0"),
    AccountType("Account type", "Universal"),
    AdvisorAccount("Advisor account", ""),

    // Reporting
    ReportRenderer("Report renderer", "com.jbooktrader.platform.report.HTMLReportRenderer"),
    ReportRecycling("Report recycling", "Append"),

    // Remote monitoring
    EmailMonitoring("Monitoring", "disabled"),
    HeartBeatInterval("Heartbeat Interval", "60"),
    SMTPSHost("SMTPS Host", "smtp.gmail.com"),
    EmailLogin("Email Login", "me@gmail.com"),
    EmailPassword("Email Password", ""),
    From("From", "me@gmail.com"),
    To("To", "me@anyprovider.com"),
    EmailSubject("Email Subject", "[JBT Remote Notification]"),

    // Back tester
    BackTesterFileName("backTester.dataFileName", ""),

    // Optimizer
    OptimizerFileName("optimizer.dataFileName", ""),
    OptimizerMinTrades("optimizer.minTrades", "50"),
    OptimizerSelectBy("optimizer.selectBy", ""),
    OptimizerMethod("optimizer.method", ""),

    // Main window
    MainWindowWidth("mainwindow.width", "-1"),
    MainWindowHeight("mainwindow.height", "-1"),
    MainWindowX("mainwindow.x", "-1"),
    MainWindowY("mainwindow.y", "-1"),

    // Performance chart
    PerformanceChartWidth("performance.chart.width", "-1"),
    PerformanceChartHeight("performance.chart.height", "-1"),
    PerformanceChartX("performance.chart.x", "-1"),
    PerformanceChartY("performance.chart.y", "-1"),
    PerformanceChartState("performance.chart.state", "-1"),

    // Optimization Map
    OptimizationMapWidth("optimization.map.width", "-1"),
    OptimizationMapHeight("optimization.map.height", "-1"),
    OptimizationMapX("optimization.map.x", "-1"),
    OptimizationMapY("optimization.map.y", "-1");

    private final String name, defaultValue;

    JBTPreferences(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getDefault() {
        return defaultValue;
    }

    public String getName() {
        return name;
    }
}
