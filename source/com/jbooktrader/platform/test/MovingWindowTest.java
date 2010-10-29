package com.jbooktrader.platform.test;

import com.jbooktrader.platform.util.*;
import org.junit.*;

public class MovingWindowTest {

    @Test
    public void testMovingWindow() {
        MovingWindow mw = new MovingWindow(6);
        int[] values = {1, 3, 4, 6, 9, 19};
        for (int value : values) {
            mw.add(value);
        }
        Assert.assertEquals(7, mw.getMean(), 0);
        Assert.assertEquals(6.48074069840786, mw.getStdev(), 0);
    }

    @Test
    public void testAccess() {
        MovingWindow mw = new MovingWindow(5);
        int[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        for (int value : values) {
            mw.add(value);
        }

        Assert.assertEquals(6, (int) mw.getElement(0));
        Assert.assertEquals(7, (int) mw.getElement(1));
        Assert.assertEquals(8, (int) mw.getElement(2));
        Assert.assertEquals(9, (int) mw.getElement(3));
        Assert.assertEquals(10, (int) mw.getElement(4));

    }
}
