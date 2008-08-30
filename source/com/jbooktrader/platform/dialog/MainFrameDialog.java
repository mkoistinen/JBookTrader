package com.jbooktrader.platform.dialog;


import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;
import static com.jbooktrader.platform.model.StrategyTableColumn.*;
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
    private final Toolkit toolkit;
    private JMenuItem exitMenuItem, aboutMenuItem, discussionMenuItem, projectHomeMenuItem, preferencesMenuItem;
    private JMenuItem infoMenuItem, tradeMenuItem, backTestMenuItem, forwardTestMenuItem, optimizeMenuItem, chartMenuItem, saveBookMenuItem;
    private StrategyTableModel strategyTableModel;
    private JTable strategyTable;
    private JPopupMenu popupMenu;

    public MainFrameDialog() {
        toolkit = Toolkit.getDefaultToolkit();
        init();
        populateStrategies();
        setVisible(true);
    }

    public void modelChanged(ModelListener.Event event, Object value) {
        switch (event) {
            case ModeChanged:
                String subTitle = "";
                switch (Dispatcher.getMode()) {
                    case Trade:
                        subTitle = "Trading";
                        break;
                    case BackTest:
                        subTitle = "Back Testing";
                        break;
                    case ForwardTest:
                        subTitle = "Forward Testing";
                        break;
                    case Optimization:
                        subTitle = "Optimizing";
                        break;

                }
                setTitle(JBookTrader.APP_NAME + " - [" + subTitle + "]");
                break;
            case Error:
                String msg = (String) value;
                MessageDialog.showError(this, msg);
                break;
            case StrategyUpdate:
                Strategy strategy = (Strategy) value;
                strategyTableModel.update(strategy);
                break;
            case StrategiesStart:
                Dispatcher.Mode mode = Dispatcher.getMode();
                if (mode == Trade) {
                    forwardTestMenuItem.setEnabled(false);
                }
                if (mode == ForwardTest) {
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

    public void strategyTableAction(MouseAdapter action) {
        strategyTable.addMouseListener(action);
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

    private URL getImageURL(String imageFileName) {
        URL imgURL = ClassLoader.getSystemResource(imageFileName);
        if (imgURL == null) {
            String msg = "Could not locate " + imageFileName + ". Make sure the /resources directory is in the classpath.";
            throw new JBookTraderException(msg);
        }
        return imgURL;
    }

    private ImageIcon getImageIcon(String imageFileName) {
        return new ImageIcon(toolkit.getImage(getImageURL(imageFileName)));
    }


    private void populateStrategies() {
        ClassFinder classFinder = new ClassFinder();
        for (Strategy strategy : classFinder.getStrategies()) {
            strategyTableModel.addStrategy(strategy);
        }
    }

    public StrategyTableModel getStrategyTableModel() {
        return strategyTableModel;
    }

    public JTable getStrategyTable() {
        return strategyTable;
    }

    public void showPopup(MouseEvent mouseEvent) {
        popupMenu.show(strategyTable, mouseEvent.getX(), mouseEvent.getY());
    }

    private void init() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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

        JScrollPane strategyTableScrollPane = new JScrollPane();
        strategyTableScrollPane.setAutoscrolls(true);
        strategyTableModel = new StrategyTableModel();
        strategyTable = new JTable(strategyTableModel);
        strategyTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // set custom column renderers
        TableColumnModel columnModel = strategyTable.getColumnModel();
        NumberRenderer nr0 = new NumberRenderer(0);
        columnModel.getColumn(NetProfit.ordinal()).setCellRenderer(nr0);
        columnModel.getColumn(MaxDD.ordinal()).setCellRenderer(nr0);
        NumberRenderer nr5 = new NumberRenderer(5);
        columnModel.getColumn(BestBid.ordinal()).setCellRenderer(nr5);
        columnModel.getColumn(BestAsk.ordinal()).setCellRenderer(nr5);

        // Make some columns wider than the rest, so that the info fits in.
        columnModel.getColumn(Strategy.ordinal()).setPreferredWidth(100);

        strategyTableScrollPane.getViewport().add(strategyTable);

        Image appIcon = Toolkit.getDefaultToolkit().getImage(getImageURL("JBookTrader.png"));
        setIconImage(appIcon);

        add(strategyTableScrollPane, BorderLayout.CENTER);
        JLabel status = new JLabel(" ");
        status.setForeground(Color.GRAY);
        add(status, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(500, 309));
        setTitle(JBookTrader.APP_NAME);
        pack();
        setLocationRelativeTo(null);
    }
}
