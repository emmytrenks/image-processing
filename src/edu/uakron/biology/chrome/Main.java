package edu.uakron.biology.chrome;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends JFrame {
    private final AtomicReference<BufferedImage> image = new AtomicReference<>(null);
    private final AtomicReference<Color> color = new AtomicReference<>(null);

    public static void main(final String[] params) {
        SwingUtilities.invokeLater(() -> {
            final Main m = new Main();
            m.display();
        });
    }

    public Main() {
        setTitle("UA Biology Image Processing");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        build();
    }

    private void build() {
        setLayout(new BorderLayout());

        final JPanel display = new JPanel();
        display.setLayout(new BorderLayout());
        final Dimension dimensionDisplay = new Dimension(1024, 576);
        display.setSize(dimensionDisplay);
        display.setMinimumSize(dimensionDisplay);
        display.setPreferredSize(dimensionDisplay);
        display.setMaximumSize(dimensionDisplay);
        display.add(new ImagePanel(dimensionDisplay.width, dimensionDisplay.height, image, color), BorderLayout.CENTER);

        final JPanel controls = new JPanel();
        controls.setLayout(new BorderLayout());
        final JPanel controlWest = new JPanel();
        controlWest.setLayout(new FlowLayout());
        final JPanel controlEast = new JPanel();
        controlEast.setLayout(new FlowLayout());

        final JLabel labelFile = new JLabel("Please select a File");

        final JFileChooser chooser = new JFileChooser();
        final JButton buttonFile = new JButton("Choose File");
        buttonFile.addActionListener(e -> {
            int returnVal = chooser.showOpenDialog(Main.this);

            if (returnVal != JFileChooser.APPROVE_OPTION) return;
            final File file = chooser.getSelectedFile();
            try {
                final BufferedImage image = ImageIO.read(file);
                if (image == null) return;
                labelFile.setText(file.getAbsolutePath());
                Main.this.image.set(image);
                SwingUtilities.invokeLater(display::repaint);
            } catch (final IOException ignored) {
            }
        });

        final JComponent buttonColor = new ColorDisplay(color);

        controlWest.add(labelFile);
        controlWest.add(buttonFile);
        controlEast.add(buttonColor);

        controls.add(controlWest, BorderLayout.WEST);
        controls.add(controlEast, BorderLayout.EAST);

        add(controls, BorderLayout.NORTH);
        add(display, BorderLayout.CENTER);
    }

    private void display() {
        pack();
        setLocationRelativeTo(getParent());
        setVisible(true);
    }
}
