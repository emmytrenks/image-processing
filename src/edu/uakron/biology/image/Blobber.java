package edu.uakron.biology.image;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

/**
 * Blobber
 * <p>
 * This is a class which provides static utilities involved in blobbing points together.
 */
public class Blobber {
    public static BufferedImage clone(final BufferedImage image) {
        final ColorModel cm = image.getColorModel();
        final boolean b = cm.isAlphaPremultiplied();
        final WritableRaster r = image.copyData(image.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, r, b, null);
    }

    public static void highlightHSL(final BufferedImage image, final Color color, final float h_mod, final float s_mod, final float tolerance, final Color foreground, final Color background) {
        final int[] m_hsl = new int[3];
        ColorUtils.RGBtoHSL(color.getRed(), color.getGreen(), color.getBlue(), m_hsl);
        final int w = image.getWidth(), h = image.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                final int rgb = image.getRGB(x, y);
                final int red = (rgb >> 16) & 0xff, green = (rgb >> 8) & 0xff, blue = rgb & 0xff;
                final int[] ints = new int[3];
                ColorUtils.RGBtoHSL(red, green, blue, ints);
                final boolean match = Math.abs(ints[0] - m_hsl[0]) <= h_mod * tolerance &&
                        Math.abs(ints[1] - m_hsl[1]) <= s_mod * tolerance &&
                        Math.abs(ints[2] - m_hsl[2]) < tolerance;
                image.setRGB(x, y, (match ? foreground : background).getRGB());
            }
        }
    }

    public static List<ArrayList<Point>> removeLowerThan(final List<ArrayList<Point>> lists, final int min_req) {
        final List<ArrayList<Point>> lists2 = new ArrayList<>();
        for (final ArrayList<Point> list : lists) {
            if (list.size() < min_req) continue;
            lists2.add(list);
        }
        return lists2;
    }

    /**
     * extractPoints takes in an image and extracts the points from the image that match the specified color.
     *
     * @param image the {@code BufferedImage} to examine
     * @param c     the {@code Color} to extract from the image
     * @return a {@code List} of {@code Point}s from the image
     */
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

    /**
     * @param image the {@code BufferedImage} to examine
     * @param c     the {@code Color} to blob in the image
     * @return a {@code List} of {@code List}s which contain the blobbed points
     */
    public static List<ArrayList<Point>> blob(final BufferedImage image, final Color c) {
        return blob(extractPoints(image, c));
    }

    /**
     * @param image the {@code BufferedImage} to examine
     * @param c     the {@code Color} to blob in the image
     * @param dist  the distance to blob points within
     * @return a {@code List} of {@code List}s which contain the blobbed points
     */
    public static List<ArrayList<Point>> blob(final BufferedImage image, final Color c, final double dist) {
        return blob(extractPoints(image, c), dist);
    }

    /**
     * @param arr the {@code List} of {@code Point}s to blob
     * @return a {@code List} of {@code List}s which contain the blobbed points
     */
    private static List<ArrayList<Point>> blob(final List<Point> arr) {
        return blob(arr, Math.sqrt(3));
    }

    /**
     * @param arr  the {@code List} of {@code Point}s to blob
     * @param dist the distance to blob points within
     * @return a {@code List} of {@code List}s which contain the blobbed points
     */
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
