package it.teilibrary.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

/**
 * Splash Window shown during long operation
 * 
 */
@SuppressWarnings("serial")
public class Splasher extends JWindow {

    private final Dimension dimScreen;
    private final JLabel text, image;
    private final JPanel panel;

    public Splasher() {
        super((Frame) null);

        Toolkit tool = Toolkit.getDefaultToolkit();
        dimScreen = tool.getScreenSize();
        text = new JLabel();
        text.setFont(new Font("SansSerif", Font.PLAIN, 10));
        text.setText("Caricamento in corso...");
        text.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        java.net.URL imageURL = this.getClass().getResource("/it/teilibrary/img/wait.gif");
        ImageIcon imageico = new ImageIcon(imageURL);

        image = new JLabel(imageico);
        image.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(new Color(204, 218, 255));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        panel.setLayout(new BorderLayout());
        panel.add(text, BorderLayout.SOUTH);
        panel.add(image, BorderLayout.CENTER);

        super.setContentPane(panel);

        super.pack();
        super.setLocation((dimScreen.width - super.getWidth()) / 2, (dimScreen.height - super.getHeight()) / 2);
        super.setAlwaysOnTop(true);
        super.setVisible(false);
    }

    public void splashHide() {
        setVisible(false);
    }

    public void splashShow() {
        setVisible(true);
    }
}
