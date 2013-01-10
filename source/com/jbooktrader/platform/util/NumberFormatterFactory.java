package com.jbooktrader.platform.util;

import java.text.*;

public class NumberFormatterFactory {

    public static DecimalFormat getNumberFormatter(int maxFractionDigits) {
        return getNumberFormatter(maxFractionDigits, false);
    }

    public static DecimalFormat getNumberFormatter(int maxFractionDigits, boolean grouping) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        DecimalFormatSymbols decimalFormatSeparator = new DecimalFormatSymbols();
        decimalFormatSeparator.setDecimalSeparator('.');
        decimalFormatSeparator.setGroupingSeparator(',');
        decimalFormat.setGroupingUsed(grouping);
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setMaximumFractionDigits(maxFractionDigits);
        decimalFormat.setDecimalFormatSymbols(decimalFormatSeparator);

        return decimalFormat;
    }

}
