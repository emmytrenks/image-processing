package edu.uakron.biology.image;

import java.awt.Color;

public class ColorUtils {
    public static void RGBtoHSL(final Color c, final int[] r) {
        RGBtoHSL(c.getRed(), c.getGreen(), c.getBlue(), r);
    }

    public static void RGBtoHSL(final int rgb, final int[] r) {
        final int red = (rgb >> 16) & 0xff, green = (rgb >> 8) & 0xff, blue = rgb & 0xff;
        RGBtoHSL(red, green, blue, r);
    }

    public static void RGBtoHSL(final int red, final int green, final int blue, final int[] r) {
        final float[] arr = new float[3];
        Color.RGBtoHSB(red, green, blue, arr);
        float h, s, l;
        h = arr[0];
        l = (2 - arr[1]) * arr[2];
        s = arr[1] * arr[2];
        s /= l <= 1 ? l : 2 - l;
        l /= 2;
        if (Double.isNaN(s)) s = 0;
        r[0] = Math.round(h * 100f);
        r[1] = Math.round(s * 100f);
        r[2] = Math.round(l * 100f);
    }
}
