package com.jbooktrader.platform.dialog;


import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.TradingTableModel.Column.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

/**
 * Main application window. All the system logic is intentionally left out if this class,
 * which acts as a simple "view" of the undelying model.
 */
public class MainFrameDialog extends JFrame implements ModelListener {
    private JMenuItem exitMenuItem, aboutMenuItem, discussionMenuItem, projectHomeMenuItem, preferencesMenuItem;
    private JMenuItem infoMenuItem, tradeMenuItem, backTestMenuItem, forwardTestMenuItem, optimizeMenuItem, chartMenuItem, saveBookMenuItem;
    private TradingTableModel tradingTableModel;
    private JTable tradingTable;
    private JPopupMenu popupMenu;
    private final Toolkit toolkit;

    public MainFrameDialog() throws JBookTraderException {
        toolkit = Toolkit.getDefaultToolkit();
        init();
        populateStrategies();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void modelChanged(ModelListener.Event event, Object value) {
        switch (event) {
            case Error:
                String msg = (String) value;
                MessageDialog.showError(this, msg);
                break;
            case StrategyUpdate:
                Strategy strategy = (Strategy) value;
                tradingTableModel.update(strategy);
                break;
            case StrategiesStart:
                Dispatcher.Mode mode = Dispatcher.getMode();
                if (mode == Dispatcher.Mode.Trade) {
                    forwardTestMenuItem.setEnabled(false);
                }
                if (mode == Dispatcher.Mode.ForwardTest) {
                    tradeMenuItem.setEnabled(false);
                }

                backTestMenuItem.setEnabled(false);
                optimizeMenuItem.setEnabled(false);
                saveBookMenuItem.setEnabled(true);
                chartMenuItem.setEnabled(true);
                break;
            case StrategiesEnd:
                forwardTestMenuItem.setEnabled(true);
                tradeMenuItem.setEnabled(true);
                backTestMenuItem.setEnabled(true);
                optimizeMenuItem.setEnabled(true);
                break;
        }
    }

    public void discussionAction(ActionListener action) {
        discussionMenuItem.addActionListener(action);
    }

    public void projectHomeAction(ActionListener action) {
        projectHomeMenuItem.addActionListener(action);
    }

    public void tradingTableAction(MouseAdapter action) {
        tradingTable.addMouseListener(action);
    }

    public void informationAction(ActionListener action) {
        infoMenuItem.addActionListener(action);
    }

    public void backTestAction(ActionListener action) {
        backTestMenuItem.addActionListener(action);
    }

    public void optimizeAction(ActionListener action) {
        optimizeMenuItem.addActionListener(action);
    }

    public void forwardTestAction(ActionListener action) {
        forwardTestMenuItem.addActionListener(action);
    }

    public void tradeAction(ActionListener action) {
        tradeMenuItem.addActionListener(action);
    }

    public void saveMarketBookAction(ActionListener action) {
        saveBookMenuItem.addActionListener(action);
    }

    public void chartAction(ActionListener action) {
        chartMenuItem.addActionListener(action);
    }

    public void preferencesAction(ActionListener action) {
        preferencesMenuItem.addActionListener(action);
    }

    public void exitAction(ActionListener action) {
        exitMenuItem.addActionListener(action);
    }

    public void exitAction(WindowAdapter action) {
        addWindowListener(action);
    }

    public void aboutAction(ActionListener action) {
        aboutMenuItem.addActionListener(action);
    }

    private URL getImageURL(String imageFileName) throws JBookTraderException {
        URL imgURL = ClassLoader.getSystemResource(imageFileName);
        if (imgURL == null) {
            String msg = "Could not locate " + imageFileName + ". Make sure the /resources directory is in the classpath.";
            throw new JBookTraderException(msg);
        }
        return imgURL;
    }

    private ImageIcon getImageIcon(String imageFileName) throws JBookTraderException {
        return new ImageIcon(toolkit.getImage(getImageURL(imageFileName)));
    }


    private void populateStrategies() throws JBookTraderException {
        ClassFinder classFinder = new ClassFinder();
        for (Strategy strategy : classFinder.getStrategies()) {
            tradingTableModel.addStrategy(strategy);
        }
    }

    public TradingTableModel getTradingTableModel() {
        return tradingTableModel;
    }

    public JTable getTradingTable() {
        return tradingTable;
    }

    public void showPopup(MouseEvent mouseEvent) {
        popupMenu.show(tradingTable, mouseEvent.getX(), mouseEvent.getY());
    }

    private void init() throws JBookTraderException {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // session menu
        JMenu sessionMenu = new JMenu("Session");
        sessionMenu.setMnemonic('S');
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic('X');
        sessionMenu.add(exitMenuItem);

        // configure menu
        JMenu configureMenu = new JMenu("Configure");
        configureMenu.setMnemonic('C');
        preferencesMenuItem = new JMenuItem("Preferences");
        preferencesMenuItem.setMnemonic('P');
        configureMenu.add(preferencesMenuItem);

        // help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');


        discussionMenuItem = new JMenuItem("Discussion Group");
        discussionMenuItem.setMnemonic('D');

        projectHomeMenuItem = new JMenuItem("Project Home");
        projectHomeMenuItem.setMnemonic('P');

        aboutMenuItem = new JMenuItem("About...");
        aboutMenuItem.setMnemonic('A');

        helpMenu.add(discussionMenuItem);
        helpMenu.add(projectHomeMenuItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutMenuItem);

        // menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(sessionMenu);
        menuBar.add(configureMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        // popup menu
        popupMenu = new JPopupMenu();

        infoMenuItem = new JMenuItem("Information", getImageIcon("information.png"));
        backTestMenuItem = new JMenuItem("Back Test", getImageIcon("backTest.png"));
        optimizeMenuItem = new JMenuItem("Optimize", getImageIcon("optimize.png"));
        forwardTestMenuItem = new JMenuItem("Forward Test", getImageIcon("forwardTest.png"));
        tradeMenuItem = new JMenuItem("Trade");
        chartMenuItem = new JMenuItem("Chart", getImageIcon("chart.png"));
        saveBookMenuItem = new JMenuItem("Save Book", getImageIcon("save.png"));

        popupMenu.add(infoMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(optimizeMenuItem);
        popupMenu.add(backTestMenuItem);
        popupMenu.add(forwardTestMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(chartMenuItem);
        popupMenu.add(saveBookMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(tradeMenuItem);

        JScrollPane tradingScroll = new JScrollPane();
        tradingScroll.setAutoscrolls(true);
        JPanel tradingPanel = new JPanel(new BorderLayout());
        tradingPanel.add(tradingScroll, BorderLayout.CENTER);

        tradingTableModel = new TradingTableModel();
        tradingTable = new JTable(tradingTableModel);
        tradingTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // set custom column renderers
        TableColumnModel columnModel = tradingTable.getColumnModel();
        NumberRenderer nr2 = new NumberRenderer(2);
        columnModel.getColumn(PL.ordinal()).setCellRenderer(nr2);

        // Make some columns wider than the rest, so that the info fits in.
        columnModel.getColumn(Strategy.ordinal()).setPreferredWidth(100);

        tradingScroll.getViewport().add(tradingTable);

        Image appIcon = Toolkit.getDefaultToolkit().getImage(getImageURL("JBookTrader.png"));
        setIconImage(appIcon);

        add(tradingPanel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(500, 309));
        setTitle(JBookTrader.APP_NAME);
        pack();
    }
}
