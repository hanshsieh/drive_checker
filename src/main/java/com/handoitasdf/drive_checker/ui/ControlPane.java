package com.handoitasdf.drive_checker.ui;

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

/**
 * Created by icand on 2017/8/31.
 */
public class ControlPane extends JPanel {

    public enum Status {
        RUNNING,
        STOPPED,
        PENDING_TO_START,
        PENDING_TO_STOP
    }

    private static final String RUNNING_BTN_TEXT = "Stop";
    private static final String STOPPED_BTN_TEXT = "Run";
    private final JFileChooser fileChooser = new JFileChooser();
    private final JTextField testFilePathField = new JTextField();
    private final JButton runBtn = new JButton();
    private final JButton selectFileBtn = new JButton("Open");
    private ControlPaneListener listener;
    private final Component parent;
    private Status status = Status.STOPPED;

    public ControlPane(@Nonnull Component parent) {
        super(new GridBagLayout());
        this.parent = parent;
        initTestFileLabel();
        initTestFilePathField();
        initSelectFileButton();
        initStartButton();
    }

    public void setControlPaneListener(@Nonnull ControlPaneListener listener) {
        this.listener = listener;
    }

    private void initStartButton() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        add(runBtn, constraints);
        runBtn.setText(STOPPED_BTN_TEXT);
        runBtn.addActionListener(e -> {
            switch (status) {
                case STOPPED:
                    pendingToStart();
                    break;
                case RUNNING:
                    pendingToStop();
                    break;
            }
        });
    }

    public void start() {
        runBtn.setText(RUNNING_BTN_TEXT);
        runBtn.setEnabled(true);
        status = Status.RUNNING;
        selectFileBtn.setEnabled(false);
    }

    public void stop() {
        runBtn.setText(STOPPED_BTN_TEXT);
        runBtn.setEnabled(true);
        status = Status.STOPPED;
        selectFileBtn.setEnabled(true);
    }

    private void pendingToStart() {
        String path = testFilePathField.getText();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Please select the test file");
            return;
        }
        File testFile = new File(testFilePathField.getText());
        if (!testFile.isFile()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "\"" + testFile.getAbsolutePath() + "\" isn't a file");
            return;
        }
        runBtn.setText(STOPPED_BTN_TEXT);
        runBtn.setEnabled(false);
        selectFileBtn.setEnabled(false);
        status = Status.PENDING_TO_START;
        if (listener != null) {
            listener.onPendingStart(testFile);
        }
    }

    private void pendingToStop() {
        status = Status.PENDING_TO_STOP;
        runBtn.setText(RUNNING_BTN_TEXT);
        runBtn.setEnabled(false);
        selectFileBtn.setEnabled(false);
        if (listener != null) {
            listener.onPendingStop();
        }
    }

    private void initTestFileLabel() {
        JLabel label = new JLabel("Test file: ");
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        add(label, constraints);
    }

    private void initSelectFileButton() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        add(selectFileBtn, constraints);

        selectFileBtn.addActionListener(e -> {
            int state = fileChooser.showOpenDialog(parent);
            if (state != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File selectedFile = fileChooser.getSelectedFile();
            testFilePathField.setText(selectedFile.getAbsolutePath());
        });
    }

    private void initTestFilePathField() {
        testFilePathField.setEditable(true);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(testFilePathField, constraints);
    }
}
