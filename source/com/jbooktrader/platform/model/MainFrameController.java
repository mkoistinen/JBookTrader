package com.jbooktrader.platform.model;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.dialog.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;
import com.jbooktrader.platform.optimizer.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Acts as a controller in the Model-View-Controller pattern
 */
public class MainFrameController {
    private final MainFrameDialog mainViewDialog;
    private final JTable strategyTable;
    private final StrategyTableModel strategyTableModel;
    private final PreferencesHolder prefs = PreferencesHolder.getInstance();

    public MainFrameController() throws JBookTraderException {
        mainViewDialog = new MainFrameDialog();
        Dispatcher.addListener(mainViewDialog);
        int width = prefs.getInt(MainWindowWidth);
        int height = prefs.getInt(MainWindowHeight);
        int x = prefs.getInt(MainWindowX);
        int y = prefs.getInt(MainWindowY);

        if (width > 0 && height > 0) {
            mainViewDialog.setBounds(x, y, width, height);
        }

        strategyTable = mainViewDialog.getStrategyTable();
        strategyTableModel = mainViewDialog.getStrategyTableModel();
        assignListeners();
    }

    private void exit() {
        String question = "Are you sure you want to exit JBookTrader?";
        int answer = JOptionPane.showConfirmDialog(mainViewDialog, question, JBookTrader.APP_NAME, JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            prefs.set(MainWindowWidth, mainViewDialog.getSize().width);
            prefs.set(MainWindowHeight, mainViewDialog.getSize().height);
            prefs.set(MainWindowX, mainViewDialog.getX());
            prefs.set(MainWindowY, mainViewDialog.getY());
            Dispatcher.exit();
        }
    }

    private Strategy createSelectedRowStrategy() throws JBookTraderException {
        int selectedRow = strategyTable.getSelectedRow();
        if (selectedRow < 0) {
            throw new JBookTraderException("No strategy is selected.");
        }
        return strategyTableModel.createStrategyForRow(selectedRow);
    }

    private void openURL(String url) {
        try {
            Browser.openURL(url);
        } catch (Throwable t) {
            Dispatcher.getReporter().report(t);
            MessageDialog.showError(mainViewDialog, t);
        }
    }

    private void assignListeners() {

        strategyTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int modifiers = e.getModifiers();
                boolean actionRequested = (modifiers & InputEvent.BUTTON2_MASK) != 0;
                actionRequested = actionRequested || (modifiers & InputEvent.BUTTON3_MASK) != 0;
                if (actionRequested) {
                    int selectedRow = strategyTable.rowAtPoint(e.getPoint());
                    strategyTable.setRowSelectionInterval(selectedRow, selectedRow);
                    mainViewDialog.showPopup(e);
                    strategyTable.setRowSelectionInterval(selectedRow, selectedRow);
                }
            }
        });

        mainViewDialog.informationAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                    int selectedRow = strategyTable.getSelectedRow();
                    if (selectedRow < 0) {
                        throw new JBookTraderException("No strategy is selected.");
                    }

                    Strategy strategy = strategyTableModel.getStrategyForRow(selectedRow);
                    if (strategy == null) {
                        String name = strategyTableModel.getStrategyNameForRow(selectedRow);
                        strategy = ClassFinder.getInstance(name);
                    }

                    new StrategyInformationDialog(mainViewDialog, strategy);
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });


        mainViewDialog.backTestAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    Dispatcher.getTrader().getAssistant().removeAllStrategies();
                    Strategy strategy = createSelectedRowStrategy();
                    Dispatcher.setMode(BackTest);
                    new BackTestDialog(mainViewDialog, strategy);
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.optimizeAction(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    int selectedRow = strategyTable.getSelectedRow();
                    if (selectedRow < 0) {
                        throw new JBookTraderException("No strategy is selected.");
                    }
                    String name = strategyTableModel.getStrategyNameForRow(selectedRow);
                    Dispatcher.setMode(Optimization);
                    new OptimizerDialog(mainViewDialog, name);
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.forwardTestAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    Strategy strategy = createSelectedRowStrategy();
                    Dispatcher.setMode(ForwardTest);
                    Dispatcher.getTrader().getAssistant().addStrategy(strategy);
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.tradeAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    Strategy strategy = createSelectedRowStrategy();
                    Dispatcher.setMode(Trade);
                    Dispatcher.getTrader().getAssistant().addStrategy(strategy);
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.chartAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    int selectedRow = strategyTable.getSelectedRow();
                    if (selectedRow < 0) {
                        throw new JBookTraderException("No strategy is selected.");
                    }

                    Strategy strategy = strategyTableModel.getStrategyForRow(selectedRow);
                    if (strategy == null) {
                        String msg = "Please run this strategy first.";
                        throw new JBookTraderException(msg);
                    }

                    if (!strategy.getPerformanceChartData().isEmpty()) {
                        PerformanceChart spChart = new PerformanceChart(mainViewDialog, strategy);
                        JFrame chartFrame = spChart.getChart();
                        chartFrame.setVisible(true);
                    } else {
                        String msg = "There is no data to chart. Please run a back test first.";
                        MessageDialog.showMessage(mainViewDialog, msg);
                    }
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.preferencesAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    new PreferencesDialog(mainViewDialog);
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });


        mainViewDialog.discussionAction(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                openURL("http://groups.google.com/group/jbooktrader/topics?gvc=2");
            }
        });

        mainViewDialog.projectHomeAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openURL("http://code.google.com/p/jbooktrader/");
            }
        });

        mainViewDialog.exitAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });

        mainViewDialog.exitAction(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        mainViewDialog.aboutAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    new AboutDialog(mainViewDialog);
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t);
                }
            }
        });
    }
}
