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
        //Assert.assertEquals(42, mw.getSum(), 0);
        Assert.assertEquals(6.48074069840786, mw.getStdev(), 0);
    }


}
