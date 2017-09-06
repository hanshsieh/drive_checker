package com.handoitasdf.drive_checker.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Created by icand on 2017/9/6.
 */
public class TrailerPane extends JPanel {
    public TrailerPane() {
        setupLayout();
        setupLogo();
    }

    private void setupLayout() {
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.RIGHT);
        setLayout(layout);
    }

    private void setupLogo() {
        JLabel label = new JLabel("CYBERNET");
        Font font = new Font("Courier", Font.BOLD,16);
        label.setFont(font);
        add(label);
    }
}
