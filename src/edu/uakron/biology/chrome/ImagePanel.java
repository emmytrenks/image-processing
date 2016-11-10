package edu.uakron.biology.chrome;

import edu.uakron.biology.image.Blobber;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
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
        final BufferedImage image = this.image.get();
        if (image == null) return;
        final Color color = this.color.get();
        if (color != null) {
            Blobber.highlightHSL(image, color, 0.8f, 0.8f, 5f, Color.white, Color.black);
        }
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
        final BufferedImage dimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = dimg.createGraphics();
        g.drawImage(drawn, 0, 0, null);
        g.dispose();
        color.set(new Color(dimg.getRGB(x, y)));
        getRootPane().repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
