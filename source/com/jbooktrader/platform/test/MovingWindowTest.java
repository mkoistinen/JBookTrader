package com.jbooktrader.platform.test;

import com.jbooktrader.platform.util.movingwindow.*;
import org.junit.*;

/**
 * @author Eugene Kononov
 */
public class MovingWindowTest {
    private final double delta = 0;

    @Test
    public void testMovingWindowEvenCapacity() {
        MovingWindow mw = new MovingWindow(4);

        Assert.assertEquals(4, mw.getCapacity());

        mw.add(1);
        Assert.assertTrue(!mw.isFull());
        mw.add(2);
        Assert.assertTrue(!mw.isFull());
        mw.add(3);
        Assert.assertTrue(!mw.isFull());
        mw.add(4);
        Assert.assertTrue(mw.isFull());
        Assert.assertEquals(1, mw.getFirst(), delta);
        Assert.assertEquals(4, mw.getLast(), delta);
        Assert.assertEquals(1, mw.get(0), delta);
        Assert.assertEquals(2, mw.get(1), delta);
        Assert.assertEquals(3, mw.get(2), delta);
        Assert.assertEquals(4, mw.get(3), delta);

        mw.add(5);
        Assert.assertTrue(mw.isFull());
        Assert.assertEquals(2, mw.getFirst(), delta);
        Assert.assertEquals(5, mw.getLast(), delta);
        Assert.assertEquals(2, mw.get(0), delta);
        Assert.assertEquals(3, mw.get(1), delta);
        Assert.assertEquals(4, mw.get(2), delta);
        Assert.assertEquals(5, mw.get(3), delta);

        mw.add(6);
        Assert.assertTrue(mw.isFull());
        Assert.assertEquals(3, mw.getFirst(), delta);
        Assert.assertEquals(6, mw.getLast(), delta);
        Assert.assertEquals(3, mw.get(0), delta);
        Assert.assertEquals(4, mw.get(1), delta);
        Assert.assertEquals(5, mw.get(2), delta);
        Assert.assertEquals(6, mw.get(3), delta);

        mw.add(7);
        Assert.assertTrue(mw.isFull());
        Assert.assertEquals(4, mw.getFirst(), delta);
        Assert.assertEquals(7, mw.getLast(), delta);
        Assert.assertEquals(4, mw.get(0), delta);
        Assert.assertEquals(5, mw.get(1), delta);
        Assert.assertEquals(6, mw.get(2), delta);
        Assert.assertEquals(7, mw.get(3), delta);

        mw.add(8);
        Assert.assertTrue(mw.isFull());
        Assert.assertEquals(5, mw.getFirst(), delta);
        Assert.assertEquals(8, mw.getLast(), delta);
        Assert.assertEquals(5, mw.get(0), delta);
        Assert.assertEquals(6, mw.get(1), delta);
        Assert.assertEquals(7, mw.get(2), delta);
        Assert.assertEquals(8, mw.get(3), delta);
    }

    @Test
    public void testMovingWindowOddCapacity() {
        MovingWindow mw = new MovingWindow(3);

        Assert.assertEquals(3, mw.getCapacity());
        Assert.assertTrue(!mw.isFull());

        mw.add(1);
        Assert.assertTrue(!mw.isFull());
        mw.add(2);
        Assert.assertTrue(!mw.isFull());
        mw.add(3);
        Assert.assertTrue(mw.isFull());
        Assert.assertEquals(1, mw.getFirst(), delta);
        Assert.assertEquals(3, mw.getLast(), delta);
        Assert.assertEquals(1, mw.get(0), delta);
        Assert.assertEquals(2, mw.get(1), delta);
        Assert.assertEquals(3, mw.get(2), delta);

        mw.add(4);
        Assert.assertTrue(mw.isFull());
        Assert.assertEquals(2, mw.getFirst(), delta);
        Assert.assertEquals(4, mw.getLast(), delta);
        Assert.assertEquals(2, mw.get(0), delta);
        Assert.assertEquals(3, mw.get(1), delta);
        Assert.assertEquals(4, mw.get(2), delta);

        mw.add(5);
        Assert.assertTrue(mw.isFull());
        Assert.assertEquals(3, mw.getFirst(), delta);
        Assert.assertEquals(5, mw.getLast(), delta);
        Assert.assertEquals(3, mw.get(0), delta);
        Assert.assertEquals(4, mw.get(1), delta);
        Assert.assertEquals(5, mw.get(2), delta);

        mw.add(6);
        Assert.assertTrue(mw.isFull());
        Assert.assertEquals(4, mw.getFirst(), delta);
        Assert.assertEquals(6, mw.getLast(), delta);
        Assert.assertEquals(4, mw.get(0), delta);
        Assert.assertEquals(5, mw.get(1), delta);
        Assert.assertEquals(6, mw.get(2), delta);

        mw.add(7);
        Assert.assertTrue(mw.isFull());
        Assert.assertEquals(5, mw.getFirst(), delta);
        Assert.assertEquals(7, mw.getLast(), delta);
        Assert.assertEquals(5, mw.get(0), delta);
        Assert.assertEquals(6, mw.get(1), delta);
        Assert.assertEquals(7, mw.get(2), delta);
    }


    @Test
    public void testMovingWindowMean() {
        MovingWindowMean mw = new MovingWindowMean(4);

        Assert.assertEquals(4, mw.getCapacity());

        mw.add(1);
        mw.add(2);
        mw.add(3);
        mw.add(4);
        Assert.assertEquals(2.5, mw.getMean(), delta);

        mw.add(-3);
        Assert.assertEquals(1.5, mw.getMean(), delta);

        mw.add(-100);
        Assert.assertEquals(-24, mw.getMean(), delta);

        mw.add(10);
        Assert.assertEquals(-22.25, mw.getMean(), delta);

        mw.add(20);
        Assert.assertEquals(-18.25, mw.getMean(), delta);
    }


    @Test
    public void testMovingWindowStdev() {
        MovingWindowStDev mw = new MovingWindowStDev(6);
        int[] values = {1, 3, 4, 6, 9, 19};
        for (int value : values) {
            mw.add(value);
        }
        Assert.assertEquals(6, mw.getCapacity());
        Assert.assertEquals(1, mw.getFirst(), delta);
        Assert.assertEquals(7, mw.getMean(), delta);
        Assert.assertEquals(6.48074069840786, mw.getStdev(), delta);

        int[] newValues = {100, 300, 500, 200, -100, -1000};
        for (int value : newValues) {
            mw.add(value);
        }
        Assert.assertEquals(6, mw.getCapacity());
        Assert.assertEquals(100, mw.getFirst(), delta);
        Assert.assertEquals(0, mw.getMean(), delta);
        Assert.assertEquals(529.1502622129182, mw.getStdev(), delta);
    }

    @Test
    public void testMovingWindowCorrelation() {
        MovingWindowCorrelation mw = new MovingWindowCorrelation(6);

        int[] x = {1, 3, 4, 6, 9, 19};
        int[] y = {100, 200, 250, 0, -10, 900, 1500};
        for (int i = 0; i < 6; i++) {
            mw.add(x[i], y[i]);
        }
        Assert.assertEquals(78.39949076271328, mw.getCorrelation(), delta);

        int[] newX = {1, 2, 3, 4, 5, 6};
        int[] newY = {-5, -6, -7, -8, -9, -8};
        for (int i = 0; i < 6; i++) {
            mw.add(newX[i], newY[i]);
        }
        Assert.assertEquals(-90.78412990032037, mw.getCorrelation(), delta);
    }

    @Test
    public void testMovingWindowMinMax() {
        MovingWindowMinMax mw = new MovingWindowMinMax(3);

        Assert.assertEquals(3, mw.getCapacity());

        mw.add(5);
        mw.add(1);
        mw.add(3);
        Assert.assertEquals(5, mw.getMax(), delta);
        Assert.assertEquals(1, mw.getMin(), delta);

        mw.add(-3);
        Assert.assertEquals(3, mw.getMax(), delta);
        Assert.assertEquals(-3, mw.getMin(), delta);

        mw.add(0);
        Assert.assertEquals(3, mw.getMax(), delta);
        Assert.assertEquals(-3, mw.getMin(), delta);

        mw.add(1);
        Assert.assertEquals(1, mw.getMax(), delta);
        Assert.assertEquals(-3, mw.getMin(), delta);

        mw.add(2);
        Assert.assertEquals(2, mw.getMax(), delta);
        Assert.assertEquals(0, mw.getMin(), delta);

        mw.add(3);
        Assert.assertEquals(3, mw.getMax(), delta);
        Assert.assertEquals(1, mw.getMin(), delta);

        mw.add(-1);
        Assert.assertEquals(3, mw.getMax(), delta);
        Assert.assertEquals(-1, mw.getMin(), delta);
    }

}
