package edu.uakron.biology.chrome;

import edu.uakron.biology.image.Blobber;
import edu.uakron.biology.image.Concave;
import edu.uakron.biology.image.QuickHull;

import javax.swing.JComponent;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ImagePanel extends JComponent implements MouseListener {
    private Image drawn = null;
    private final int w, h;
    private final AtomicReference<BufferedImage> image;
    private final AtomicReference<Color> color;

    public ImagePanel(final int w, final int h, final AtomicReference<BufferedImage> image, final AtomicReference<Color> color) {
        this.w = w;
        this.h = h;
        this.image = image;
        this.color = color;

        addMouseListener(this);
    }

    @Override
    public void paintComponent(final Graphics g) {
        BufferedImage image = this.image.get();
        if (image == null) return;
        image = Blobber.clone(image);
        final Graphics gr = image.getGraphics();
        final Color color = this.color.get();
        if (color != null) {
            final BufferedImage high = Blobber.clone(image);
            System.out.println("Highlighting image ...");
            Blobber.highlightHSL(high, color, 0.8f, 0.8f, 30f, Color.white, Color.black);
            System.out.println("Blobbing points and removing insignificant blobs ...");
            final List<ArrayList<Point>> lists = Blobber.removeLowerThan(Blobber.blob(high, Color.white), 20);
            System.out.println("Found " + lists.size() + " blobs ...");
            int count = 1;
            for (final ArrayList<Point> list : lists) {
                System.out.println("Generating convex hull for blob " + (count++) + "/" + lists.size() + ".");
                Polygon p = QuickHull.generate(list);
                gr.setColor(Color.red);
                gr.drawPolygon(p);
                System.out.println("Generating concave hull ...");
                p = Concave.hull(p, list, 20);
                gr.setColor(Color.blue);
                gr.drawPolygon(p);
            }
        }
        gr.dispose();
        final Image r = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        this.drawn = r;
        g.drawImage(r, 0, 0, null);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        final int x = e.getX(), y = e.getY();
        if (drawn == null) return;
        final BufferedImage dimg = getBuffered(drawn);
        color.set(new Color(dimg.getRGB(x, y)));
        getRootPane().repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private BufferedImage getBuffered(Image image) {
        final BufferedImage dimg = new BufferedImage(
                image.getWidth(null),
                image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics g = dimg.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return dimg;
    }
}
