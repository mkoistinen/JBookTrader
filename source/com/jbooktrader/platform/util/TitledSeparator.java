package com.jbooktrader.platform.util;

import javax.swing.*;
import java.awt.*;


public class TitledSeparator extends JPanel {
    public TitledSeparator(Component component) {
        component.setFont(component.getFont().deriveFont(Font.BOLD));
        setLayout(new GridBagLayout());
        add(component);
        GridBagConstraints constrants = new GridBagConstraints();
        constrants.anchor = GridBagConstraints.WEST;
        constrants.fill = GridBagConstraints.HORIZONTAL;
        constrants.weightx = 1;
        constrants.insets = new Insets(0, 0, 0, 12);
        add(new JSeparator(), constrants);
    }
}

/* $Id$ */
