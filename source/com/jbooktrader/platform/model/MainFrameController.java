package com.jbooktrader.platform.model;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.dialog.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.classfinder.*;
import com.jbooktrader.platform.util.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * Acts as a controller in the Model-View-Controller pattern
 *
 * @author Eugene Kononov
 */
public class MainFrameController {
    private final MainFrameDialog mainViewDialog;
    private final JTable strategyTable;
    private final StrategyTableModel strategyTableModel;
    private final Dispatcher dispatcher;

    public MainFrameController() throws JBookTraderException {
        mainViewDialog = new MainFrameDialog();
        dispatcher = Dispatcher.getInstance();
        dispatcher.addListener(mainViewDialog);
        strategyTable = mainViewDialog.getStrategyTable();
        strategyTableModel = mainViewDialog.getStrategyTableModel();
        assignListeners();
    }

    private void exit() {
        String question = "Are you sure you want to exit " + JBookTrader.APP_NAME + "?";
        int answer = JOptionPane.showConfirmDialog(mainViewDialog, question, JBookTrader.APP_NAME, JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            PreferencesHolder prefs = PreferencesHolder.getInstance();
            prefs.set(MainWindowWidth, mainViewDialog.getSize().width);
            prefs.set(MainWindowHeight, mainViewDialog.getSize().height);
            dispatcher.exit();
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
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(url));
        } catch (Throwable t) {
            dispatcher.getEventReport().report(t);
            MessageDialog.showException(t);
        }
    }

    private void assignListeners() {

        strategyTable.addMouseListener(new MouseAdapter() {
            @Override
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
                    MessageDialog.showException(t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });


        mainViewDialog.backTestAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    dispatcher.getTrader().getAssistant().removeAllStrategies();
                    Strategy strategy = createSelectedRowStrategy();
                    dispatcher.setMode(Mode.BackTest);
                    new BackTestDialog(mainViewDialog, strategy);
                } catch (Throwable t) {
                    MessageDialog.showException(t);
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
                    dispatcher.setMode(Mode.Optimization);
                    OptimizerDialog optimizerDialog = new OptimizerDialog(mainViewDialog, name);
                    optimizerDialog.setVisible(true);
                } catch (Throwable t) {
                    MessageDialog.showException(t);
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
                    dispatcher.setMode(Mode.ForwardTest);
                    dispatcher.getTrader().getAssistant().addStrategy(strategy);
                } catch (Throwable t) {
                    MessageDialog.showException(t);
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
                    dispatcher.setMode(Mode.Trade);
                    dispatcher.getTrader().getAssistant().addStrategy(strategy);
                } catch (Throwable t) {
                    MessageDialog.showException(t);
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
                        MessageDialog.showMessage("No strategy is selected.");
                        return;
                    }

                    Strategy strategy = strategyTableModel.getStrategyForRow(selectedRow);
                    if (strategy == null) {
                        String msg = "Please run this strategy first.";
                        MessageDialog.showMessage(msg);
                        return;
                    }

                    PerformanceChartData pcd = strategy.getPerformanceManager().getPerformanceChartData();
                    if (pcd == null || pcd.isEmpty()) {
                        String msg = "There is no data to chart. Please run a back test first.";
                        MessageDialog.showMessage(msg);
                        return;
                    }

                    PerformanceChart spChart = new PerformanceChart(mainViewDialog, strategy);
                    JFrame chartFrame = spChart.getChart();
                    chartFrame.setVisible(true);
                } catch (Throwable t) {
                    MessageDialog.showException(t);
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
                    MessageDialog.showException(t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.suspendLiveTradingAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object[] options = {"Suspend live trading", "Close positions and suspend live trading", "Cancel"};
                int selectedOption = JOptionPane.showOptionDialog(mainViewDialog, "What would you like to do?",
                    JBookTrader.APP_NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[2]);

                try {
                    switch (selectedOption) {
                        case 0:
                            Dispatcher.getInstance().setMode(Mode.ForwardTest);
                            MessageDialog.showMessage("Live trading suspended. Running mode set to Forward Test");
                            break;
                        case 1:
                            Dispatcher.getInstance().setMode(Mode.ForceClose);
                            MessageDialog.showMessage("Request to close open positions and to stop trading has been sent.");
                            break;
                        case 2:
                            // nothing to do, user cancelled
                            break;
                    }
                } catch (Exception ex) {
                    MessageDialog.showException(ex);
                }
            }
        });

        mainViewDialog.discussionAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openURL("http://groups.google.com/group/jbooktrader/topics?gvc=2");
            }
        });

        mainViewDialog.releaseNotesAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openURL("http://code.google.com/p/jbooktrader/wiki/ReleaseNotes");
            }
        });

        mainViewDialog.userManualAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openURL("http://docs.google.com/View?id=dfzgvqp4_10gb63b8hg");
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
                    MessageDialog.showException(t);
                }
            }
        });
    }
}
