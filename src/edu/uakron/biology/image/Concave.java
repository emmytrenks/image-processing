package edu.uakron.biology.image;

import java.awt.Point;
import java.awt.Polygon;
import java.util.*;

public class Concave {
    private static double MAX_CONCAVE_ANGLE_COS = Math.cos(90.0 / (180.0 / Math.PI)); // angle = 90 deg
    @SuppressWarnings("FieldCanBeLocal")
    private static double MAX_SEARCH_BBOX_SIZE_PERCENT = 0.6;

    private static class Grid {
        private Map<Integer, Map<Integer, LinkedList<int[]>>> _cells = new HashMap<>();
        private final double _cellSize;

        public Grid(final List<Point> points, double cellSize) {
            this._cellSize = cellSize;
            for (final Point p : points) {
                int x = p.x, y = p.y;
                Map<Integer, LinkedList<int[]>> map1 = _cells.get(x);
                if (map1 == null) {
                    _cells.put(x, map1 = new HashMap<>());
                }
                LinkedList<int[]> list1 = map1.get(y);
                if (list1 == null) {
                    map1.put(y, list1 = new LinkedList<>());
                }
                list1.add(new int[]{x, y});
            }
        }

        private int[] point2CellXY(double[] point) { // (Array) -> Array
            int x = (int) (point[0] / this._cellSize),
                    y = (int) (point[1] / this._cellSize);
            return new int[]{x, y};
        }

        private int[] point2CellXY(int[] point) { // (Array) -> Array
            return point2CellXY(new double[]{point[0], point[1]});
        }

        int[][] rangePoints(double[] bbox) { // (Array) -> Array
            int[] tlCellXY = this.point2CellXY(new double[]{bbox[0], bbox[1]}),
                    brCellXY = this.point2CellXY(new double[]{bbox[2], bbox[3]});
            List<int[]> points = new LinkedList<>();

            for (int x = tlCellXY[0]; x <= brCellXY[0]; x++) {
                for (int y = tlCellXY[1]; y <= brCellXY[1]; y++) {
                    LinkedList<int[]> pts = _cells.getOrDefault(x, new HashMap<>()).getOrDefault(y, new LinkedList<>());
                    points.addAll(pts);
                }
            }
            int[][] _points = new int[points.size()][];
            points.toArray(_points);
            return _points;
        }

        void removePoint(int[] point) {
            int[] cellXY = this.point2CellXY(point);
            LinkedList<int[]> _cell = _cells.getOrDefault(cellXY[0], new HashMap<>()).getOrDefault(cellXY[1], new LinkedList<>());
            int pointIdxInCell = -1;
            int[][] cell = new int[_cell.size()][];
            _cell.toArray(cell);
            for (int i = 0; i < cell.length; i++) {
                if (cell[i][0] == point[0] && cell[i][1] == point[1]) {
                    pointIdxInCell = i;
                    break;
                }
            }
            if (pointIdxInCell >= 0) _cell.remove(pointIdxInCell);
        }

        double[] extendBbox(double[] bbox, double scaleFactor) { // (Array, Number) -> Array
            return new double[]{
                    bbox[0] - (scaleFactor * this._cellSize),
                    bbox[1] - (scaleFactor * this._cellSize),
                    bbox[2] + (scaleFactor * this._cellSize),
                    bbox[3] + (scaleFactor * this._cellSize)
            };
        }
    }

    private static boolean ccw(int x1, int y1, int x2, int y2, int x3, int y3) {
        int cw = ((y3 - y1) * (x2 - x1)) - ((y2 - y1) * (x3 - x1));
        return cw > 0 || cw >= 0; // colinear
    }

    private static boolean intersect(int[][] seg1, int[][] seg2) {
        int x1 = seg1[0][0], y1 = seg1[0][1],
                x2 = seg1[1][0], y2 = seg1[1][1],
                x3 = seg2[0][0], y3 = seg2[0][1],
                x4 = seg2[1][0], y4 = seg2[1][1];

        return ccw(x1, y1, x3, y3, x4, y4) != ccw(x2, y2, x3, y3, x4, y4) && ccw(x1, y1, x2, y2, x3, y3) != ccw(x1, y1, x2, y2, x4, y4);
    }

    private static boolean _intersect(int[][] segment, Polygon pointSet) {
        for (int i = 0; i < pointSet.npoints - 1; i++) {
            int[][] seg = {
                    {pointSet.xpoints[i], pointSet.ypoints[i]},
                    {pointSet.xpoints[i + 1], pointSet.ypoints[i + 1]}
            };
            if (segment[0][0] == seg[0][0] && segment[0][1] == seg[0][1] ||
                    segment[0][0] == seg[1][0] && segment[0][1] == seg[1][1]) {
                continue;
            }
            if (intersect(segment, seg)) {
                return true;
            }
        }
        return false;
    }

    private static String join(int[] arr) {
        String str = "";
        for (int i = 0; i < arr.length - 2; ++i) {
            str += arr[i] + ",";
        }
        str += arr[arr.length - 1];
        return str;
    }

    private static double _sqLength(int[] a, int[] b) {
        return Math.pow(b[0] - a[0], 2) + Math.pow(b[1] - a[1], 2);
    }

    private static double _cos(int[] o, int[] a, int[] b) {
        int[] aShifted = {a[0] - o[0], a[1] - o[1]}, bShifted = {b[0] - o[0], b[1] - o[1]};
        double sqALen = _sqLength(o, a),
                sqBLen = _sqLength(o, b),
                dot = aShifted[0] * bShifted[0] + aShifted[1] * bShifted[1];
        return dot / Math.sqrt(sqALen * sqBLen);
    }


    private static int[] _midPoint(int[][] edge, int[][] innerPoints, Polygon convex) {
        int[] point = null;
        double angle1Cos = MAX_CONCAVE_ANGLE_COS,
                angle2Cos = MAX_CONCAVE_ANGLE_COS,
                a1Cos, a2Cos;

        for (int[] innerPoint : innerPoints) {
            a1Cos = _cos(edge[0], edge[1], innerPoint);
            a2Cos = _cos(edge[1], edge[0], innerPoint);

            if (a1Cos > angle1Cos && a2Cos > angle2Cos &&
                    !_intersect(new int[][]{edge[0], innerPoint}, convex) &&
                    !_intersect(new int[][]{edge[1], innerPoint}, convex)) {
                angle1Cos = a1Cos;
                angle2Cos = a2Cos;
                point = innerPoint;
            }
        }
        return point;
    }

    private static double[] _bBoxAround(int[][] edge) {
        return new double[]{
                Math.min(edge[0][0], edge[1][0]), // left
                Math.min(edge[0][1], edge[1][1]), // top
                Math.max(edge[0][0], edge[1][0]), // right
                Math.max(edge[0][1], edge[1][1])  // bottom
        };
    }


    private static Polygon _hull(
            Polygon convex,
            double maxSqEdgeLen,
            double[] maxSearchArea,
            Grid grid,
            Map<String, Boolean> edgeSkipList
    ) {
        boolean midPointInserted = false;
        for (int i = 0; i < convex.npoints - 1; i++) {
            int[][] edge = {
                    {convex.xpoints[i], convex.ypoints[i]},
                    {convex.xpoints[i + 1], convex.ypoints[i + 1]}
            };
            String keyInSkipList = join(edge[0]) + "," + join(edge[1]);

            if (_sqLength(edge[0], edge[1]) < maxSqEdgeLen ||
                    edgeSkipList.getOrDefault(keyInSkipList, false)) {
                continue;
            }

            int scaleFactor = 0;
            int[] midPoint;
            double[] bBoxAround = _bBoxAround(edge);
            double bBoxWidth, bBoxHeight;
            do {
                bBoxAround = grid.extendBbox(bBoxAround, scaleFactor);
                bBoxWidth = bBoxAround[2] - bBoxAround[0];
                bBoxHeight = bBoxAround[3] - bBoxAround[1];
                midPoint = _midPoint(edge, grid.rangePoints(bBoxAround), convex);
                scaleFactor++;
            } while (midPoint == null && (maxSearchArea[0] > bBoxWidth || maxSearchArea[1] > bBoxHeight));

            if (bBoxWidth >= maxSearchArea[0] && bBoxHeight >= maxSearchArea[1]) {
                edgeSkipList.put(keyInSkipList, true);
            }

            if (midPoint != null) {
                Polygon nConvex = new Polygon();
                for (int i2 = 0; i2 < convex.npoints; ++i2) {
                    nConvex.addPoint(convex.xpoints[i2], convex.ypoints[i2]);
                    if (i2 == i) nConvex.addPoint(midPoint[0], midPoint[1]);
                }
                convex = nConvex;
                grid.removePoint(midPoint);
                midPointInserted = true;
            }
        }

        if (midPointInserted) {
            return _hull(convex, maxSqEdgeLen, maxSearchArea, grid, edgeSkipList);
        }

        return convex;
    }

    private static int[] _occupiedArea(Polygon pointSet) {
        int minX = Integer.MAX_VALUE,
                minY = Integer.MAX_VALUE,
                maxX = Integer.MIN_VALUE,
                maxY = Integer.MIN_VALUE;

        for (int i = pointSet.npoints - 1; i >= 0; i--) {
            if (pointSet.xpoints[i] < minX) {
                minX = pointSet.xpoints[i];
            }
            if (pointSet.ypoints[i] < minY) {
                minY = pointSet.ypoints[i];
            }
            if (pointSet.xpoints[i] > maxX) {
                maxX = pointSet.xpoints[i];
            }
            if (pointSet.ypoints[i] > maxY) {
                maxY = pointSet.ypoints[i];
            }
        }

        return new int[]{
                maxX - minX, // width
                maxY - minY  // height
        };
    }


    public static Polygon hull(Polygon convex, List<Point> points, int maxEdgeLen) {
        final Map<String, Boolean> m = new HashMap<>();
        int[] occupiedArea = _occupiedArea(convex);
        double[] maxSearchArea = {
                occupiedArea[0] * MAX_SEARCH_BBOX_SIZE_PERCENT,
                occupiedArea[1] * MAX_SEARCH_BBOX_SIZE_PERCENT
        };
        List<Point> innerPoints = new LinkedList<>(points);
        for (int i = 0; i < convex.npoints; ++i) {
            Point rem = new Point(convex.xpoints[i], convex.ypoints[i]);
            innerPoints.remove(rem);
        }
        double cellSize = Math.ceil(1.0 / (points.size() / (double) (occupiedArea[0] * occupiedArea[1])));
        if (cellSize == 0) cellSize = 1;
        return _hull(convex, Math.pow(maxEdgeLen, 2),
                maxSearchArea, new Grid(innerPoints, cellSize), m);
    }
}
