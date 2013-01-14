package com.jbooktrader.platform.dialog;

import javax.swing.*;
import java.awt.*;

/**
 * @author Eugene Kononov
 */
public class JBTDialog extends JDialog {
    public static final double MAC_MENUBAR_HEIGHT = 22;

    public JBTDialog(Frame owner) {
        super(owner);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        // This is to prevent the dialog from being drawn under the Mac menu bar.
        boolean onMac = System.getProperty("os.name").toLowerCase().startsWith("mac os x");
        if (onMac) {
            Point point = getLocation();
            if (point.getY() < MAC_MENUBAR_HEIGHT) {
                point.translate(0, (int) (MAC_MENUBAR_HEIGHT - point.getY()));
            }
            setLocation(point);
        }
    }
}
