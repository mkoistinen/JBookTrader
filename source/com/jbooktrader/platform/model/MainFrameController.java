package com.jbooktrader.platform.model;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.chart.StrategyPerformanceChart;
import com.jbooktrader.platform.dialog.*;
import com.jbooktrader.platform.marketdepth.MarketBook;
import com.jbooktrader.platform.optimizer.OptimizerDialog;
//import com.jbooktrader.platform.startup.JBookTrader;
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
    private final JTable tradingTable;
    private final TradingTableModel tradingTableModel;

    public MainFrameController() throws JBookTraderException {
        mainViewDialog = new MainFrameDialog();
        tradingTable = mainViewDialog.getTradingTable();
        tradingTableModel = mainViewDialog.getTradingTableModel();
        assignListeners();
    }

    private Strategy getSelectedRowStrategy() throws JBookTraderException {
        int selectedRow = tradingTable.getSelectedRow();
        if (selectedRow < 0) {
            throw new JBookTraderException("No strategy is selected.");
        }
        return tradingTableModel.getStrategyForRow(selectedRow);
    }

    private Strategy createSelectedRowStrategy() throws JBookTraderException {
        int selectedRow = tradingTable.getSelectedRow();
        if (selectedRow < 0) {
            throw new JBookTraderException("No strategy is selected.");
        }
        return tradingTableModel.createStrategyForRow(selectedRow);
    }

    private void openURL(String url) {
        try {
            Browser.openURL(url);
        } catch (Throwable t) {
            Dispatcher.getReporter().report(t);
            MessageDialog.showError(mainViewDialog, t.getMessage());
        }
    }

    private void assignListeners() {

        tradingTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int modifiers = e.getModifiers();
                boolean actionRequested = (modifiers & InputEvent.BUTTON2_MASK) != 0;
                actionRequested = actionRequested || (modifiers & InputEvent.BUTTON3_MASK) != 0;
                if (actionRequested) {
                    int selectedRow = tradingTable.rowAtPoint(e.getPoint());
                    tradingTable.setRowSelectionInterval(selectedRow, selectedRow);
                    mainViewDialog.showPopup(e);
                    tradingTable.setRowSelectionInterval(selectedRow, selectedRow);
                }
            }
        });


        mainViewDialog.backTestAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    Strategy strategy = createSelectedRowStrategy();
                    Dispatcher.setMode(Dispatcher.Mode.BACK_TEST);
                    new BackTestDialog(mainViewDialog, strategy);
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t.getMessage());
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.optimizeAction(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    Strategy strategy = getSelectedRowStrategy();
                    Dispatcher.setMode(Dispatcher.Mode.OPTIMIZATION);
                    new OptimizerDialog(mainViewDialog, strategy.getName());
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t.getMessage());
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
                    Dispatcher.setMode(Dispatcher.Mode.FORWARD_TEST);
                    new Thread(new StrategyRunner(strategy)).start();
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t.getMessage());
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
                    Dispatcher.setMode(Dispatcher.Mode.TRADE);
                    new Thread(new StrategyRunner(strategy)).start();
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t.getMessage());
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.saveMarketBookAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    Strategy strategy = getSelectedRowStrategy();
                    MarketBook book = strategy.getMarketBook();
                    if (book.size() != 0) {
                        BackTestFileWriter backTestFileWriter = new BackTestFileWriter(strategy.getTradingSchedule().getTimeZone());
                        backTestFileWriter.write(book);
                    } else {
                        String msg = "The book for this strategy is empty. Please run a strategy first.";
                        MessageDialog.showMessage(mainViewDialog, msg);
                    }
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t.toString());
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }

            }
        });

        mainViewDialog.chartAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    Strategy strategy = getSelectedRowStrategy();
                    MarketBook book = strategy.getMarketBook();
                    if (book.size() != 0) {
                        StrategyPerformanceChart spChart = new StrategyPerformanceChart(strategy);
                        JFrame chartFrame = spChart.getChartFrame(mainViewDialog);
                        chartFrame.setVisible(true);
                    } else {
                        String msg = "There is no data to chart. Please run a strategy first.";
                        MessageDialog.showMessage(mainViewDialog, msg);
                    }
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t.getMessage());
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
                Dispatcher.exit();
                mainViewDialog.dispose();
            }
        });

        mainViewDialog.exitAction(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Dispatcher.exit();
            }
        });

        mainViewDialog.aboutAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    new AboutDialog(mainViewDialog);
                } catch (Throwable t) {
                    MessageDialog.showError(mainViewDialog, t.getMessage());
                }
            }
        });
    }
}
