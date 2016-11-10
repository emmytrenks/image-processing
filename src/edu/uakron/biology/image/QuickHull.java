package edu.uakron.biology.image;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuickHull {
    public static double[] centers(final Polygon polygon) {
        double cx = 0, cy = 0;
        for (int i = 0; i < polygon.npoints; i++) {
            cx += polygon.xpoints[i];
            cy += polygon.ypoints[i];
        }
        cx /= polygon.npoints;
        cy /= polygon.npoints;
        return new double[]{cx, cy};
    }

    public static Polygon rotate(final Polygon polygon, final double cx, final double cy, final double angle) {
        final Polygon p = new Polygon();
        for (int i = 0; i < polygon.npoints; i++) {
            final double x = polygon.xpoints[i] - cx, y = polygon.ypoints[i] - cy;
            final double nx = x * Math.cos(angle) - y * Math.sin(angle), ny = x * Math.sin(angle) + y * Math.cos(angle);
            p.addPoint((int) Math.round(cx + nx), (int) Math.round(cy + ny));
        }
        return p;
    }

    public static Polygon generate(final ArrayList<Point> points) {
        final Polygon p = new Polygon();
        if (points.size() < 3) return p;
        final List<Point> xSorted = (ArrayList<Point>) points.clone();
        Collections.sort(xSorted, (o1, o2) -> {
            final int x = o1.x, x2 = o2.x;
            return x < x2 ? -1 : x == x2 ? 0 : 1;
        });
        final int n = points.size();
        final Point[] upper = new Point[n], lower = new Point[n];
        upper[0] = xSorted.get(0);
        upper[1] = xSorted.get(1);
        int ptr1 = 2;
        for (int i = 2; i < n; i++) {
            upper[ptr1++] = xSorted.get(i);
            while (ptr1 > 2 && !rightTurn(upper[ptr1 - 3], upper[ptr1 - 2], upper[ptr1 - 1])) {
                upper[ptr1 - 2] = upper[ptr1-- - 1];
            }
        }
        lower[0] = xSorted.get(n - 1);
        lower[1] = xSorted.get(n - 2);
        int ptr2 = 2;
        for (int i = n - 3; i >= 0; i--) {
            lower[ptr2++] = xSorted.get(i);
            while (ptr2 > 2 && !rightTurn(lower[ptr2 - 3], lower[ptr2 - 2], lower[ptr2 - 1])) {
                lower[ptr2 - 2] = lower[ptr2-- - 1];
            }
        }
        for (int i = 0; i < ptr1; i++) {
            p.addPoint(upper[i].x, upper[i].y);
        }
        for (int i = 0; i < ptr2; i++) {
            p.addPoint(lower[i].x, lower[i].y);
        }
        return p;
    }

    private static boolean rightTurn(Point a, Point b, Point c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x) > 0;
    }
}
