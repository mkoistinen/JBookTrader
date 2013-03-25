package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.util.format.*;

/**
 * A string wrapper class that has a sorting algorithm for Doubles (including positive infinity).
 *
 * @author Eugene Kononov
 */
public class DoubleNumericString implements Comparable<DoubleNumericString> {
    private final String value;
    private final static String infinity = NumberFormatterFactory.getNumberFormatter(2).format(Double.POSITIVE_INFINITY);

    public DoubleNumericString(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(DoubleNumericString other) {
        if (value.equals(infinity)) {
            return 1;
        } else if (other.toString().equals(infinity)) {
            return -1;
        } else {
            return Double.valueOf(value).compareTo(Double.valueOf(other.toString()));
        }
    }

    @Override
    public String toString() {
        return value;
    }
}



