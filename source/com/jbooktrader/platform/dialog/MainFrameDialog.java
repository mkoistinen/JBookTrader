package com.jbooktrader.platform.dialog;


import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.classfinder.*;
import com.jbooktrader.platform.util.ui.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.text.*;

import static com.jbooktrader.platform.model.StrategyTableColumn.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * Main application window. All the system logic is intentionally left out if this class,
 * which acts strictly as a "view" of the underlying model.
 *
 * @author Eugene Kononov
 */
public class MainFrameDialog extends JFrame implements ModelListener {
    private final Toolkit toolkit;
    private JLabel timeLabel;
    private JMenuItem exitMenuItem, suspendLiveTradingMenuItem, aboutMenuItem, userManualMenuItem, discussionMenuItem, releaseNotesMenuItem, projectHomeMenuItem, preferencesMenuItem;
    private JMenuItem infoMenuItem, tradeMenuItem, backTestMenuItem, forwardTestMenuItem, optimizeMenuItem, chartMenuItem;
    private StrategyTableModel strategyTableModel;
    private JTable strategyTable;
    private JPopupMenu popupMenu;
    private SimpleDateFormat sdf;

    public MainFrameDialog() throws JBookTraderException {
        toolkit = Toolkit.getDefaultToolkit();
        init();
        populateStrategies();
        PreferencesHolder prefs = PreferencesHolder.getInstance();
        int width = prefs.getInt(MainWindowWidth);
        int height = prefs.getInt(MainWindowHeight);
        pack();
        setSize(width, height);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void modelChanged(Event event, Object value) {
        switch (event) {
            case ModeChanged:
                Mode mode = Dispatcher.getInstance().getMode();
                if (mode == Mode.Trade) {
                    suspendLiveTradingMenuItem.setEnabled(true);
                } else {
                    suspendLiveTradingMenuItem.setEnabled(false);
                }
                if (mode == Mode.Trade || mode == Mode.ForwardTest) {
                    backTestMenuItem.setEnabled(false);
                    optimizeMenuItem.setEnabled(false);
                    chartMenuItem.setEnabled(false);
                    if (mode == Mode.Trade) {
                        forwardTestMenuItem.setEnabled(false);
                    }
                    if (mode == Mode.ForwardTest) {
                        tradeMenuItem.setEnabled(false);
                    }
                }
                if (mode == Mode.BackTest || mode == Mode.Optimization) {
                    forwardTestMenuItem.setEnabled(false);
                    tradeMenuItem.setEnabled(false);
                }
                setTitle(JBookTrader.APP_NAME + " - [" + mode.getName() + "]");
                break;
            case Error:
                String msg = (String) value;
                MessageDialog.showError(msg);
                break;
            case TimeUpdate:
                timeLabel.setText(sdf.format(value));
                break;
            case StrategyUpdate:
                strategyTableModel.update((Strategy) value);
                break;
            case ExpirationUpdate:
                strategyTableModel.expirationUpdate((Strategy) value);
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

    public void suspendLiveTradingAction(ActionListener action) {
        suspendLiveTradingMenuItem.addActionListener(action);
    }

    public void exitAction(WindowAdapter action) {
        addWindowListener(action);
    }

    public void aboutAction(ActionListener action) {
        aboutMenuItem.addActionListener(action);
    }

    private URL getImageURL(String imageFileName) throws JBookTraderException {
        URL imgURL = getClass().getResource("/resources/" + imageFileName); // load from a jar
        if (imgURL == null) {
            imgURL = getClass().getResource("/" + imageFileName); // load from a directory
        }

        if (imgURL == null) {
            String msg = "Could not locate file: " + imageFileName + ". Make sure the /resources directory is in the classpath.";
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
        suspendLiveTradingMenuItem = new JMenuItem("Suspend Live Trading");
        suspendLiveTradingMenuItem.setEnabled(false);
        exitMenuItem = new JMenuItem("Exit", 'X');
        sessionMenu.add(suspendLiveTradingMenuItem);
        sessionMenu.addSeparator();
        sessionMenu.add(exitMenuItem);

        // configure menu
        JMenu configureMenu = new JMenu("Configure");
        configureMenu.setMnemonic('C');
        preferencesMenuItem = new JMenuItem("Preferences...", 'P');
        configureMenu.add(preferencesMenuItem);

        // help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        userManualMenuItem = new JMenuItem("User Manual", 'U');
        releaseNotesMenuItem = new JMenuItem("Release Notes", 'R');
        discussionMenuItem = new JMenuItem("Discussion Group", 'D');
        projectHomeMenuItem = new JMenuItem("Project Home", 'P');
        aboutMenuItem = new JMenuItem("About...", 'A');
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
        columnModel.getColumn(Strategy.ordinal()).setPreferredWidth(175);

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
        setTitle(JBookTrader.APP_NAME);

        setMinimumSize(new Dimension(600, 200));
    }
}
