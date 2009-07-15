package com.jbooktrader.platform.preferences;

public enum JBTPreferences {
    // TWS connection
    Host("Host", "localhost"),
    Port("Port", "7496"),
    ClientID("Client ID", "0"),

    // Web Access
    WebAccess("Web Access", "disabled"),
    WebAccessPort("Web Access Port", "1234"),
    WebAccessUser("Web Access User", "admin"),
    WebAccessPassword("Web Access Password", "admin"),
    WebAccessTableLayout("Table Layout", "simple"),

    // Back tester
    BackTesterFileName("backTester.dataFileName", ""),

    // Optimizer
    OptimizerMinTrades("optimizer.minTrades", "50"),
    OptimizerSelectBy("optimizer.selectBy", ""),
    OptimizerMethod("optimizer.method", ""),
    OptimizerWindowWidth("optimizerwindow.width", "-1"),
    OptimizerWindowHeight("optimizerwindow.height", "-1"),
    OptimizerWindowX("optimizerwindow.x", "-1"),
    OptimizerWindowY("optimizerwindow.y", "-1"),

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

    // Optimizer
    DivideAndConquerCoverage("Divide & Conquer coverage", "10"),

    // Optimization Map
    OptimizationMapWidth("optimization.map.width", "-1"),
    OptimizationMapHeight("optimization.map.height", "-1"),
    OptimizationMapX("optimization.map.x", "-1"),
    OptimizationMapY("optimization.map.y", "-1"),

    // Collective2
    Collective2Password("C2 Password", ""),
    Collective2Strategies("C2 Strategies", ""),

    // Look & Feel
    LookAndFeel("Look & Feel", "Substance"),
    Skin("Skin", "Mist Aqua");


    private final String name, defaultValue;

    private JBTPreferences(String name, String defaultValue) {
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
