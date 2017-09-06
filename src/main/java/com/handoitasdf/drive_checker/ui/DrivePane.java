package com.handoitasdf.drive_checker.ui;

import com.handoitasdf.drive_checker.CheckingStatus;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    // Put an arbitrary character so that the vertical space can be preserved.
    private static final String EMPTY_TEXT = " ";
    private static final int CHECK_BOX_PAD_Y = 10;
    private JCheckBox checkBox = new JCheckBox();
    private JLabel statusLabel = new JLabel();
    private JLabel iterationCountLabel = new JLabel();
    private JLabel copiedSizeLabel = new JLabel();
    private final File drive;
    private DrivePaneListener listener;

    public DrivePane(@Nonnull File drive) {
        this.drive = drive;
        setLayout(new GridBagLayout());
        setupCheckBox();
        setupDriveLabel();
        setupStatusLabel();
        setupCopiedSizeLabel();
        setupIterationCountLabel();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (checkBox.isEnabled()) {
                    checkBox.setSelected(!checkBox.isSelected());
                }
            }
        });
    }

    public void setListener(@Nullable DrivePaneListener listener) {
        this.listener = listener;
    }

    private void setupCheckBox() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.ipady = CHECK_BOX_PAD_Y;
        constraints.anchor = GridBagConstraints.NORTH;
        checkBox.addItemListener(e -> {
            if (listener != null) {
                listener.onSelectionChanged(checkBox.isSelected());
            }
        });

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
        statusLabel.setText(EMPTY_TEXT);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        add(statusLabel, constraints);
    }

    private void setupIterationCountLabel() {
        clearIterationCount();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        add(iterationCountLabel, constraints);
    }

    private void setupCopiedSizeLabel() {
        clearCopiedSize();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        add(copiedSizeLabel, constraints);
    }

    public void setCheckStatus(@Nonnull CheckingStatus status) {
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
                statusLabel.setText(EMPTY_TEXT);
                break;
        }
    }

    public void setCopiedSize(long bytes) {
        copiedSizeLabel.setText(FileUtils.byteCountToDisplaySize(bytes));
    }

    public void clearCopiedSize() {
        copiedSizeLabel.setText(EMPTY_TEXT);
    }

    public void setIterationCount(int iterationCount) {
        iterationCountLabel.setText("round: " + iterationCount);
    }

    public void clearIterationCount() {
        iterationCountLabel.setText(EMPTY_TEXT);
    }

    @Override
    public void setEnabled(boolean enabled) {
        checkBox.setEnabled(enabled);
    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    public void setSelected(boolean selected) {
        checkBox.setSelected(selected);
    }

    @Nonnull
    public File getDrive() {
        return drive;
    }
}
