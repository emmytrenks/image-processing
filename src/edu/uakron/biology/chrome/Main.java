package edu.uakron.biology.chrome;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class Main extends JFrame {
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

        final JPanel controls = new JPanel();
        controls.setLayout(new BorderLayout());
        final JPanel controlWest = new JPanel();
        controlWest.setLayout(new FlowLayout());
        final JPanel controlEast = new JPanel();
        controlEast.setLayout(new FlowLayout());

        final JLabel labelFile = new JLabel("Please select a File");
        final JButton buttonFile = new JButton("Choose File");
        final JButton buttonColor = new JButton("Choose Color");
        controlWest.add(labelFile);
        controlWest.add(buttonFile);
        controlEast.add(buttonColor);

        controls.add(controlWest, BorderLayout.WEST);
        controls.add(controlEast, BorderLayout.EAST);

        final JPanel display = new JPanel();
        display.setLayout(new BorderLayout());
        final Dimension dimensionDisplay = new Dimension(1024, 576);
        display.setSize(dimensionDisplay);
        display.setMinimumSize(dimensionDisplay);
        display.setPreferredSize(dimensionDisplay);
        display.setMaximumSize(dimensionDisplay);

        add(controls, BorderLayout.NORTH);
        add(display, BorderLayout.CENTER);
    }

    private void display() {
        pack();
        setLocationRelativeTo(getParent());
        setVisible(true);
    }
}
