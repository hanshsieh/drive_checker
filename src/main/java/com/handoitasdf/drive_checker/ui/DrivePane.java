package com.handoitasdf.drive_checker.ui;

import javax.annotation.Nonnull;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Created by icand on 2017/9/1.
 */
public class DrivePane extends JPanel {

    public enum CheckStatus {
        SUCCESS,
        FAILED,
        RUNNING,
        CANCELED,
        NONE
    }

    // Put an arbitrary character so that the vertical space can be preserved.
    private static final String DEFAULT_STATUS_TEXT = " ";
    private static final int CHECK_BOX_PAD_Y = 10;
    private JCheckBox checkBox = new JCheckBox();
    private JLabel statusLabel = new JLabel();
    private final File drive;

    public DrivePane(@Nonnull File drive) {
        this.drive = drive;
        setLayout(new GridBagLayout());
        setupCheckBox();
        setupDriveLabel();
        setupStatusLabel();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                checkBox.setSelected(!checkBox.isSelected());
            }
        });
    }

    private void setupCheckBox() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.ipady = CHECK_BOX_PAD_Y;
        constraints.anchor = GridBagConstraints.NORTH;
        add(checkBox, constraints);
    }

    private void setupDriveLabel() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        DriveLabel driveLabel = new DriveLabel(drive);
        add(driveLabel, constraints);
    }

    private void setupStatusLabel() {
        statusLabel.setText(DEFAULT_STATUS_TEXT);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        add(statusLabel, constraints);
    }

    public void setCheckStatus(@Nonnull CheckStatus status) {
        switch (status) {
            case SUCCESS:
                statusLabel.setText("Success");
                statusLabel.setForeground(new Color(0, 200, 0));
                break;
            case FAILED:
                statusLabel.setText("Failed");
                statusLabel.setForeground(Color.RED);
                break;
            case RUNNING:
                statusLabel.setText("Running");
                statusLabel.setForeground(Color.GRAY);
                break;
            case CANCELED:
                statusLabel.setText("Canceled");
                statusLabel.setForeground(Color.GRAY);
                break;
            default:
                statusLabel.setText(DEFAULT_STATUS_TEXT);
                break;
        }
    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    @Nonnull
    public File getDrive() {
        return drive;
    }
}
