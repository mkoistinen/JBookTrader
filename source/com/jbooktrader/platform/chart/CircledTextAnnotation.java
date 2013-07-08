package com.jbooktrader.platform.chart;

import org.jfree.chart.annotations.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.ui.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * Defines the shape of the markers which show strategy positions on the
 * performance chart. In this implementation, the shape of a marker is a
 * solid circle whose color indicates the position taken (long, short, or flat)
 *
 * @author Eugene Kononov
 */
public class CircledTextAnnotation extends AbstractXYAnnotation {
    private static final int ANNOTATION_RADIUS = 5;
    private final Color fillColor;
    private final double x, y;

    public CircledTextAnnotation(int quantity, double x, double y) {
        this.x = x;
        this.y = y;
        fillColor = (quantity > 0) ? Color.GREEN : ((quantity < 0) ? Color.RED : Color.YELLOW);
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

        double anchorX = domainAxis.valueToJava2D(x, dataArea, domainEdge);
        double anchorY = rangeAxis.valueToJava2D(y, dataArea, rangeEdge);

        anchorX -= ANNOTATION_RADIUS;
        anchorY -= ANNOTATION_RADIUS;
        double width = ANNOTATION_RADIUS * 2.0;
        double height = ANNOTATION_RADIUS * 2.0;

        g2.setColor(fillColor);
        g2.fill(new Ellipse2D.Double(anchorX, anchorY, width, height));

        g2.setPaint(Color.GRAY);
        g2.draw(new Ellipse2D.Double(anchorX, anchorY, width, height));

    }
}
