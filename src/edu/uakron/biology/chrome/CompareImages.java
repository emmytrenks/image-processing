package edu.uakron.biology.chrome;

import edu.uakron.biology.image.Blobber;
import edu.uakron.biology.image.QuickHull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class CompareImages {
    private static final double SCALE_FACTOR = 0.5;
    private static final Color color = new Color(48, 43, 38);
    private static final double SEED_SIZE = 12;
    private static final double MAX_TRAVEL = SEED_SIZE / 2;
    private static final int CONSIDER_SEED = 10;

    private static Point midpoint(final Polygon p) {
        long x = 0, y = 0;
        for (int n = 0; n < p.npoints; ++n) {
            x += p.xpoints[n];
            y += p.ypoints[n];
        }
        return new Point((int) x / p.npoints, (int) y / p.npoints);
    }

    public static void main(final String[] params) throws IOException {
        if (params.length < 2) {
            System.err.println("You must specify at least two images.");
            System.exit(1);
        }

        final List<Point> baseline = new ArrayList<>();
        int width = -1, height = -1;
        for (int imageIndex = 0; imageIndex < params.length; ++imageIndex) {
            System.out.println("Analyzing image " + (imageIndex + 1) + ".");
            final BufferedImage before = ImageIO.read(new File(params[imageIndex]));
            final int w = before.getWidth(), h = before.getHeight();
            if (width < 0) {
                width = w;
                height = h;
            } else if (width != w || height != h) throw new RuntimeException("Image dimension mismatch!");
            BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(SCALE_FACTOR, SCALE_FACTOR);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            after = scaleOp.filter(before, after);

            final BufferedImage high = Blobber.clone(after);
            Blobber.highlightHSL(high, color, 0.8f, 0.8f, 15f, Color.white, Color.black);
            final java.util.List<ArrayList<Point>> lists = Blobber.removeLowerThan(Blobber.blob(high, Color.white, SEED_SIZE), CONSIDER_SEED);
            System.out.println(lists.size());
            double delta = 0;
            for (final ArrayList<Point> list : lists) {
                final Polygon p = QuickHull.generate(list);
                final Point mp = midpoint(p);
                if (imageIndex == 0) baseline.add(mp);
                else {
                    double d = Double.MAX_VALUE;
                    for (final Point bp : baseline) {
                        final double d2 = bp.distance(mp);
                        if (d2 < d) {
                            d = d2;
                        }
                    }
                    if (d <= MAX_TRAVEL) {
                        //System.out.println("Point delta: " + d);
                        delta += d;
                    }
                }
            }
            System.out.println("Delta: " + delta);
        }
    }
}
