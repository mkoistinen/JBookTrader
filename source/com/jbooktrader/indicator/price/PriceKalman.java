package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;
import jama.*;
import jkalman.*;

public class PriceKalman extends Indicator {
    private final JKalman kalman;
    private final Matrix m;

    public PriceKalman(int size) {
        super(size);
        try {
            kalman = new JKalman(size, size);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        m = new Matrix(1, 1);
        kalman.setTransition_matrix(Matrix.identity(1, 1));
        kalman.setError_cov_post(kalman.getError_cov_post().identity());

    }

    @Override
    public void reset() {
        value = marketBook.getSnapshot().getPrice();
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        kalman.Predict();
        m.set(0, 0, price);
        kalman.Correct(m);
        value = kalman.getState_post().get(0, 0);
    }
}
