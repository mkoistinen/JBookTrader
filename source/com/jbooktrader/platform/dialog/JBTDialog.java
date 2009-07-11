package com.jbooktrader.platform.dialog;

import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.awt.*;


public class JBTDialog extends JDialog {

    public JBTDialog(Frame owner) {
        super(owner);
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);

        // This is to prevent the dialog from being drawn under the Mac menu bar.
        try {
            if (JBookTrader.onMac()) {
                Point point = getLocation();
                if (point.getY() < JBookTrader.MAC_MENUBAR_HEIGHT) {
                    point.translate(0, (int) (JBookTrader.MAC_MENUBAR_HEIGHT - point.getY()));
                }
                setLocation(point);
            }
        }
        catch (Exception e) {
            MessageDialog.showMessage(null, e.getMessage());
        }
    }
}
