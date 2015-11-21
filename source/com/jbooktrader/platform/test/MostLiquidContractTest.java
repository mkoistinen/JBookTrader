package com.jbooktrader.platform.test;

import com.jbooktrader.platform.util.contract.*;
import org.junit.*;

import java.util.*;

/**
 * unit tests for com.jbooktrader.platform.marketbook.MarketBook
 *
 * @author Eugene Kononov
 */
public class MostLiquidContractTest {

    @Test
    public void testCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2007);
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 12);
        String mostLiquid = MostLiquidContract.getMostLiquid(calendar);
        Assert.assertEquals("200709", mostLiquid);
    }

    @Test
    public void testNextMonthRollover() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2007);
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 13);
        String mostLiquid = MostLiquidContract.getMostLiquid(calendar);
        Assert.assertEquals("200712", mostLiquid);
    }

    @Test
    public void testNextYearRollover() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2006);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 13);
        String mostLiquid = MostLiquidContract.getMostLiquid(calendar);
        Assert.assertEquals("200703", mostLiquid);
    }

}
