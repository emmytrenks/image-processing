package edu.uakron.biology.chrome;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.concurrent.atomic.AtomicReference;

public class ColorDisplay extends JComponent {
    private final AtomicReference<Color> color;

    public ColorDisplay(final AtomicReference<Color> color) {
        final Dimension d = new Dimension(200, 50);
        setSize(d);
        setPreferredSize(d);
        setMinimumSize(d);

        this.color = color;
    }

    @Override
    public void paintComponent(final Graphics g) {
        final Color color = this.color.get();
        if (color == null) return;
        g.setColor(color);
        final int w = getWidth(), h = getHeight();
        g.fillRect(0, 0, w, h);
        g.drawRect(0, 0, w, h);
    }
}
