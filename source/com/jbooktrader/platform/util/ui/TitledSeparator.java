package com.jbooktrader.platform.util.ui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Eugene Kononov
 */
public class TitledSeparator extends JPanel {
    public TitledSeparator(Component component) {
        component.setFont(component.getFont().deriveFont(Font.BOLD));
        setLayout(new GridBagLayout());
        add(component);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(0, 6, 0, 10);

        add(new JSeparator(), constraints);
    }
}
