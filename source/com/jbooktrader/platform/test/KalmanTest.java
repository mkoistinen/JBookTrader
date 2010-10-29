package com.jbooktrader.platform.test;

import jama.*;
import jkalman.*;
import org.junit.*;


public class KalmanTest {

    @Test
    public void test() throws Exception {
        JKalman kalman = new JKalman(1, 1, 1);
        Matrix m = new Matrix(1, 1); // measurement
        kalman.setTransition_matrix(Matrix.identity(1, 1));
        kalman.setError_cov_post(kalman.getError_cov_post().identity());

        kalman.Predict();
        m.set(0, 0, 100);
        kalman.Correct(m);
        Assert.assertEquals((int) kalman.getState_post().get(0, 0), 90);

        kalman.Predict();
        m.set(0, 0, 50);
        kalman.Correct(m);
        Assert.assertEquals((int) kalman.getState_post().get(0, 0), 71);
    }
}
