package com.jbooktrader.platform.dialog;


import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.StrategyTableColumn.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.text.*;

/**
 * Main application window. All the system logic is intentionally left out if this class,
 * which acts as a simple "view" of the underlying model.
 */
public class MainFrameDialog extends JFrame implements ModelListener {
    private final Toolkit toolkit;
    private JLabel timeLabel;
    private JMenuItem exitMenuItem, aboutMenuItem, userManualMenuItem, discussionMenuItem, releaseNotesMenuItem, projectHomeMenuItem, preferencesMenuItem;
    private JMenuItem infoMenuItem, tradeMenuItem, backTestMenuItem, forwardTestMenuItem, optimizeMenuItem, chartMenuItem;
    private StrategyTableModel strategyTableModel;
    private JTable strategyTable;
    private JPopupMenu popupMenu;
    private SimpleDateFormat sdf;

    public MainFrameDialog() throws JBookTraderException {
        toolkit = Toolkit.getDefaultToolkit();
        init();
        populateStrategies();
        setVisible(true);
    }

    public void modelChanged(Event event, Object value) {
        switch (event) {
            case ModeChanged:
                String subTitle = Dispatcher.getInstance().getMode().getName();
                setTitle(JBookTrader.APP_NAME + " - [" + subTitle + "]");
                break;
            case Error:
                String msg = (String) value;
                MessageDialog.showError(msg);
                break;
            case TimeUpdate:
                timeLabel.setText(sdf.format(value));
                break;
            case StrategyUpdate:
                Strategy strategy = (Strategy) value;
                strategyTableModel.update(strategy);
                break;
            case StrategiesStart:
                Mode mode = Dispatcher.getInstance().getMode();
                if (mode == Mode.Trade) {
                    forwardTestMenuItem.setEnabled(false);
                }
                if (mode == Mode.ForwardTest) {
                    tradeMenuItem.setEnabled(false);
                }

                backTestMenuItem.setEnabled(false);
                optimizeMenuItem.setEnabled(false);
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

    public void userManualAction(ActionListener action) {
        userManualMenuItem.addActionListener(action);
    }

    public void discussionAction(ActionListener action) {
        discussionMenuItem.addActionListener(action);
    }

    public void releaseNotesAction(ActionListener action) {
        releaseNotesMenuItem.addActionListener(action);
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


    private void populateStrategies() {
        for (Strategy strategy : ClassFinder.getStrategies()) {
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

    private void init() throws JBookTraderException {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        sdf = new SimpleDateFormat("HH:mm:ss");

        // session menu
        JMenu sessionMenu = new JMenu("Session");
        sessionMenu.setMnemonic('S');
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic('X');
        sessionMenu.add(exitMenuItem);

        // configure menu
        JMenu configureMenu = new JMenu("Configure");
        configureMenu.setMnemonic('C');
        preferencesMenuItem = new JMenuItem("Preferences...");
        preferencesMenuItem.setMnemonic('P');
        configureMenu.add(preferencesMenuItem);

        // help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        userManualMenuItem = new JMenuItem("User Manual");
        userManualMenuItem.setMnemonic('U');
        releaseNotesMenuItem = new JMenuItem("Release Notes");
        releaseNotesMenuItem.setMnemonic('R');
        discussionMenuItem = new JMenuItem("Discussion Group");
        discussionMenuItem.setMnemonic('D');
        projectHomeMenuItem = new JMenuItem("Project Home");
        projectHomeMenuItem.setMnemonic('P');
        aboutMenuItem = new JMenuItem("About...");
        aboutMenuItem.setMnemonic('A');
        helpMenu.add(userManualMenuItem);
        helpMenu.addSeparator();
        helpMenu.add(releaseNotesMenuItem);
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

        popupMenu.add(infoMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(optimizeMenuItem);
        popupMenu.add(backTestMenuItem);
        popupMenu.add(forwardTestMenuItem);
        popupMenu.add(chartMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(tradeMenuItem);

        JScrollPane strategyTableScrollPane = new JScrollPane();
        strategyTableScrollPane.setAutoscrolls(true);
        strategyTableModel = new StrategyTableModel();
        strategyTable = new JTable(strategyTableModel);
        strategyTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        strategyTable.setShowGrid(false);

        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) strategyTable.getDefaultRenderer(String.class);
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);

        // Make some columns wider than the rest, so that the info fits in.
        TableColumnModel columnModel = strategyTable.getColumnModel();
        columnModel.getColumn(Strategy.ordinal()).setPreferredWidth(100);
        columnModel.getColumn(DepthBalance.ordinal()).setPreferredWidth(120);
        columnModel.getColumn(ProfitFactor.ordinal()).setPreferredWidth(100);

        strategyTableScrollPane.getViewport().add(strategyTable);

        Image appIcon = Toolkit.getDefaultToolkit().getImage(getImageURL("JBookTrader.png"));
        setIconImage(appIcon);

        add(strategyTableScrollPane, BorderLayout.CENTER);

        JToolBar statusBar = new JToolBar();
        statusBar.setLayout(new BorderLayout());
        statusBar.setFloatable(false);
        timeLabel = new JLabel(" ");

        statusBar.add(timeLabel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(600, 200));
        setTitle(JBookTrader.APP_NAME);
        pack();
        setLocationRelativeTo(null);
    }
}
