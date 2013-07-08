package com.jbooktrader.platform.dialog;

import com.ib.client.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.trader.*;
import com.jbooktrader.platform.util.ui.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Dialog to show the application info, system info, and IB API info.
 *
 * @author Eugene Kononov
 */
public class AboutDialog extends JBTDialog {

    /* inner class to define the "about" model */
    private class AboutTableModel extends TableDataModel {
        private AboutTableModel() {
            String[] aboutSchema = {"Property", "Value"};
            setSchema(aboutSchema);
        }
    }

    public AboutDialog(JFrame parent) {
        super(parent);
        init();
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void init() {
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("About " + JBookTrader.APP_NAME);

        JPanel contentPanel = new JPanel(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPanel.add(tabbedPane1, BorderLayout.CENTER);

        JPanel aboutPanel = new JPanel(new SpringLayout());
        JPanel apiPanel = new JPanel(new SpringLayout());

        tabbedPane1.addTab("About", aboutPanel);
        tabbedPane1.addTab("API Info", apiPanel);

        JLabel productLabel = new JLabel("Product:", SwingConstants.TRAILING);
        JLabel productValueLabel = new JLabel(JBookTrader.APP_NAME);
        productValueLabel.setForeground(Color.BLACK);
        productLabel.setLabelFor(productValueLabel);
        aboutPanel.add(productLabel);
        aboutPanel.add(productValueLabel);

        JLabel versionLabel = new JLabel("Version:", SwingConstants.TRAILING);
        JLabel versionValueLabel = new JLabel(JBookTrader.VERSION);
        versionValueLabel.setForeground(Color.BLACK);
        versionLabel.setLabelFor(versionValueLabel);
        aboutPanel.add(versionLabel);
        aboutPanel.add(versionValueLabel);

        JLabel releaseDateLabel = new JLabel("Released:", SwingConstants.TRAILING);
        JLabel releaseDateValueLabel = new JLabel(JBookTrader.RELEASE_DATE);
        releaseDateValueLabel.setForeground(Color.BLACK);
        releaseDateLabel.setLabelFor(releaseDateValueLabel);
        aboutPanel.add(releaseDateLabel);
        aboutPanel.add(releaseDateValueLabel);

        JLabel copyrightLabel = new JLabel("Copyright:", SwingConstants.TRAILING);
        JLabel copyrightValueLabel = new JLabel(JBookTrader.COPYRIGHT);
        copyrightValueLabel.setForeground(Color.BLACK);
        copyrightLabel.setLabelFor(copyrightValueLabel);
        aboutPanel.add(copyrightLabel);
        aboutPanel.add(copyrightValueLabel);

        SpringUtilities.makeCompactGrid(aboutPanel, 4, 2, 12, 12, 5, 5);

        JLabel serverVersionLabel = new JLabel("Server Version:", SwingConstants.TRAILING);
        String serverVersion = "Disconnected from server";
        Trader trader = Dispatcher.getInstance().getTrader();
        if (trader != null) {
            int version = trader.getAssistant().getServerVersion();
            if (version != 0) {
                serverVersion = String.valueOf(version);
            }
        }
        JLabel serverVersionValueLabel = new JLabel(serverVersion);
        serverVersionValueLabel.setForeground(Color.BLACK);
        serverVersionLabel.setLabelFor(serverVersionValueLabel);
        apiPanel.add(serverVersionLabel);
        apiPanel.add(serverVersionValueLabel);

        JLabel clientVersionLabel = new JLabel("Client Version:", SwingConstants.TRAILING);
        JLabel clientVersionValueLabel = new JLabel("" + EClientSocket.CLIENT_VERSION);
        clientVersionValueLabel.setForeground(Color.BLACK);
        clientVersionLabel.setLabelFor(clientVersionValueLabel);
        apiPanel.add(clientVersionLabel);
        apiPanel.add(clientVersionValueLabel);

        SpringUtilities.makeCompactGrid(apiPanel, 2, 2, 12, 12, 5, 5);

        JPanel systemInfoPanel = new JPanel(new BorderLayout(5, 5));
        tabbedPane1.addTab("System Info", systemInfoPanel);

        JScrollPane systemInfoScrollPane = new JScrollPane();
        systemInfoPanel.add(systemInfoScrollPane, BorderLayout.CENTER);

        TableDataModel aboutModel = new AboutTableModel();
        systemInfoScrollPane.getViewport().add(new JTable(aboutModel));

        getContentPane().setPreferredSize(new Dimension(450, 400));

        Properties properties = System.getProperties();
        Enumeration<?> propNames = properties.propertyNames();

        while (propNames.hasMoreElements()) {
            String key = (String) propNames.nextElement();
            String value = properties.getProperty(key);
            String[] row = {key, value};
            aboutModel.addRow(row);
        }
    }
}
