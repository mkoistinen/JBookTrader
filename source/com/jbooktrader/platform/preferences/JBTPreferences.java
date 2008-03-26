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
    EmailAddress("Email Address", "@gmail.com"),
    EmailPassword("Password", ""),
    EmailSubject("Email Subject", "[JBT Remote Notification]"),
    // Back tester
    BackTesterFileName("backTester.dataFileName", ""),
    // Optimizer
    OptimizerFileName("optimizer.dataFileName", ""),
    OptimizerMinTrades("optimizer.minTrades", "20"),
    OptimizerSelectBy("optimizer.selectBy", ""),
    OptimizerMethod("optimizer.method", ""),
    // Main window
    MainWindowWidth("mainwindow.width", "-1"),
    MainWindowHeight("mainwindow.height", "-1"),
    MainWindowX("mainwindow.x", "-1"),
    MainWindowY("mainwindow.y", "-1"),
    // Chart
    ChartWidth("chart.width", "-1"),
    ChartHeight("chart.height", "-1"),
    ChartX("chart.x", "-1"),
    ChartY("chart.y", "-1"),
    ChartState("chart.state", "-1");

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
