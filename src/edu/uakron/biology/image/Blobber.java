package edu.uakron.biology.image;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Blobber {
    private static ArrayList<Point> extractPoints(final BufferedImage image, final Color c) {
        final ArrayList<Point> p = new ArrayList<>();
        final int rgb = c.getRGB();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) == rgb) p.add(new Point(x, y));
            }
        }
        return p;
    }

    public static List<ArrayList<Point>> blob(final BufferedImage image, final Color c) {
        return blob(extractPoints(image, c));
    }

    public static List<ArrayList<Point>> blob(final BufferedImage image, final Color c, final double dist) {
        return blob(extractPoints(image, c), dist);
    }

    private static List<ArrayList<Point>> blob(final List<Point> arr) {
        return blob(arr, Math.sqrt(3));
    }

    private static List<ArrayList<Point>> blob(final List<Point> arr, final double dist) {
        final List<ArrayList<Point>> result = new ArrayList<>();
        final int l = arr.size() - 1;
        if (l < 0) return result;
        int c = 0, ec = 0;
        while (l - ec >= 0) {
            final ArrayList<Point> list = new ArrayList<>(100);
            result.add(c, list);
            list.add(arr.get(0));
            arr.set(0, arr.get(l - ec++));
            int tc = 1, t1 = 0;
            while (t1 < tc) {
                int t2 = 0;
                while (t2 <= l - ec) {
                    final Point p = result.get(c).get(t1), p2 = arr.get(t2);
                    final int v1 = Math.abs(p.x - p2.x), v2 = Math.abs(p.y - p2.y);
                    if (Math.sqrt(v1 * v1 + v2 * v2) <= dist) {
                        list.add(tc, arr.get(t2));
                        arr.set(t2, arr.get(l - ec));
                        ec++;
                        tc++;
                        t2--;
                    }
                    t2++;
                }
                t1++;
            }
            c++;
        }
        return result;
    }
}
