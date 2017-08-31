package com.handoitasdf.drive_checker.ui;

import com.handoitasdf.drive_checker.DriveChecker;

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by icand on 2017/8/31.
 */
public class ControlPane extends JPanel {
    private final JFileChooser fileChooser = new JFileChooser();
    private final JTextField testFilePathField = new JTextField();
    private ControlPaneListener listener;
    private final Component parent;

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
        JButton button = new JButton("Run");
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        add(button, constraints);
        button.addActionListener(e -> {
            File testFile = new File(testFilePathField.getText());
            if (!testFile.isFile()) {
                JOptionPane.showMessageDialog(
                        parent,
                        "Fail to open file \"" + testFile.getPath() + "\"");
                return;
            }
            listener.onRun(testFile);
        });
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
        JButton button = new JButton("Open");
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        add(button, constraints);

        button.addActionListener(e -> {
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
