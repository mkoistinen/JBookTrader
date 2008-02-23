package com.jbooktrader.platform.util;

import javax.swing.table.DefaultTableCellRenderer;
import java.text.*;


public class DoubleRenderer extends DefaultTableCellRenderer {
    private final DecimalFormat df;

    public DoubleRenderer(int precision) {
        df = (DecimalFormat) NumberFormat.getNumberInstance();
        df.setMaximumFractionDigits(precision);
        df.setGroupingUsed(false);
        setHorizontalAlignment(RIGHT);
    }

    @Override
    public void setValue(Object value) {
        String text = "";
        if (value != null) {
            if (!Double.isInfinite((Double) value) && !Double.isNaN((Double) value)) {
                text = df.format(value);
            }
        }
        setText(text);
    }
}
