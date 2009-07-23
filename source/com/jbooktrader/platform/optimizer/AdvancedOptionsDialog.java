package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.dialog.*;
import com.jbooktrader.platform.preferences.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class AdvancedOptionsDialog extends JBTDialog {
    private static final Dimension FIELD_DIMENSION = new Dimension(Integer.MAX_VALUE, 22);
    private final PreferencesHolder prefs;
    private JSlider divideAndConquerCoverageSlider;
    private JTextField bruteForceStrategiesPerProcessorText;

    public AdvancedOptionsDialog(JFrame parent) {
        super(parent);
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        setLocationRelativeTo(null);
        setModal(true);
        setVisible(true);
    }

    private void add(JPanel panel, JBTPreferences pref, JTextField textField) {
        textField.setText(prefs.get(pref));
        genericAdd(panel, pref, textField, FIELD_DIMENSION);
    }

    private void genericAdd(JPanel panel, JBTPreferences pref, Component comp, Dimension dimension) {
        JLabel fieldNameLabel = new JLabel(pref.getName() + ":");
        fieldNameLabel.setLabelFor(comp);
        comp.setPreferredSize(dimension);
        comp.setMaximumSize(dimension);
        panel.add(fieldNameLabel);
        panel.add(comp);
    }

    private void genericAdd(JPanel panel, JBTPreferences pref, Component comp) {
        genericAdd(panel, pref, comp, null);
    }


    private void add(JPanel panel, JBTPreferences pref, JSlider slider) {
        slider.setValue(prefs.getInt(pref));
        genericAdd(panel, pref, slider);
    }

    private void init() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Advanced Optimization Options");

        JPanel contentPanel = new JPanel(new SpringLayout());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 12));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        bruteForceStrategiesPerProcessorText = new JTextField();
        bruteForceStrategiesPerProcessorText.setHorizontalAlignment(JTextField.RIGHT);
        add(contentPanel, JBTPreferences.BruteForceStrategiesPerProcessor, bruteForceStrategiesPerProcessorText);


        int min = 1;
        int max = 50;
        divideAndConquerCoverageSlider = new JSlider(min, max);
        divideAndConquerCoverageSlider.setMajorTickSpacing(1);
        divideAndConquerCoverageSlider.setPaintTicks(true);
        divideAndConquerCoverageSlider.setSnapToTicks(true);
        Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
        Font labelFont = divideAndConquerCoverageSlider.getFont().deriveFont(Font.ITALIC, 12);
        JLabel sparserLabel = new JLabel("Sparser");
        sparserLabel.setFont(labelFont);
        JLabel denserLabel = new JLabel("Denser");
        denserLabel.setFont(labelFont);
        labels.put(min, sparserLabel);
        labels.put(max, denserLabel);
        divideAndConquerCoverageSlider.setLabelTable(labels);
        divideAndConquerCoverageSlider.setPaintLabels(true);
        add(contentPanel, DivideAndConquerCoverage, divideAndConquerCoverageSlider);

        SpringUtilities.makeCompactGrid(contentPanel, 2, 2, 12, 12, 6, 8);


        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prefs.set(DivideAndConquerCoverage, divideAndConquerCoverageSlider.getValue());
                prefs.set(BruteForceStrategiesPerProcessor, bruteForceStrategiesPerProcessorText.getText());
                dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });


        getRootPane().setDefaultButton(okButton);
        setPreferredSize(new Dimension(650, 380));
    }

}
