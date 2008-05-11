package com.jbooktrader.platform.dialog;

import com.ib.client.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.trader.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Dialog to show the application info, system info, and IB API info.
 */
public class AboutDialog extends JDialog {

    /* inner class to define the "about" model */
    private class AboutTableModel extends TableDataModel {
        public AboutTableModel() {
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

        JLabel productLabel = new JLabel("Product:", JLabel.TRAILING);
        JLabel productValueLabel = new JLabel(JBookTrader.APP_NAME);
        productValueLabel.setForeground(Color.BLACK);
        productLabel.setLabelFor(productValueLabel);
        aboutPanel.add(productLabel);
        aboutPanel.add(productValueLabel);

        JLabel versionLabel = new JLabel("Version:", JLabel.TRAILING);
        JLabel versionValueLabel = new JLabel(JBookTrader.VERSION);
        versionValueLabel.setForeground(Color.BLACK);
        versionLabel.setLabelFor(versionValueLabel);
        aboutPanel.add(versionLabel);
        aboutPanel.add(versionValueLabel);

        JLabel authorLabel = new JLabel("Author:", JLabel.TRAILING);
        JLabel authorValueLabel = new JLabel("Eugene Kononov");
        authorValueLabel.setForeground(Color.BLACK);
        authorLabel.setLabelFor(authorValueLabel);
        aboutPanel.add(authorLabel);
        aboutPanel.add(authorValueLabel);

        JLabel emailLabel = new JLabel("Email:", JLabel.TRAILING);
        JLabel emailValueLabel = new JLabel("nonlinear5@yahoo.com");
        emailValueLabel.setForeground(Color.BLACK);
        emailLabel.setLabelFor(productValueLabel);
        aboutPanel.add(emailLabel);
        aboutPanel.add(emailValueLabel);

        JLabel licenseLabel = new JLabel("License:", JLabel.TRAILING);
        JLabel licenseValueLabel = new JLabel("BSD (Free, open source)");
        licenseValueLabel.setForeground(Color.BLACK);
        licenseLabel.setLabelFor(licenseValueLabel);
        aboutPanel.add(licenseLabel);
        aboutPanel.add(licenseValueLabel);

        SpringUtilities.makeCompactGrid(aboutPanel, 5, 2, 12, 12, 5, 5);

        JLabel serverVersionLabel = new JLabel("Server Version:", JLabel.TRAILING);
        String serverVersion = "Disconnected from server";
        Trader trader = Dispatcher.getTrader();
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

        JLabel clientVersionLabel = new JLabel("Client Version:", JLabel.TRAILING);
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
