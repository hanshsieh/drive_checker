package com.handoitasdf.drive_checker.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application entry point.
 */
public class App {
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private final JFrame frame = new JFrame("Drive Checker");
    private final DrivesPane drivesPane = new DrivesPane();
    private final ControlPane controlPane = new ControlPane(frame.getContentPane());
    private DrivesCheckWorker drivesCheckWorker = null;

    public App() {
        initFrame();
    }

    private void initFrame() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        initFrameContent();
        frame.setVisible(true);
    }

    private void initFrameContent() {
        FlowLayout contentLayout = new FlowLayout();
        contentLayout.setAlignment(FlowLayout.LEFT);
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());

        initControlPane();

        contentPane.add(drivesPane, BorderLayout.CENTER);
    }

    private void initControlPane() {
        controlPane.setControlPaneListener(new ControlPaneListener() {
            @Override
            public void onPendingStart(@Nonnull File testFile) {
                checkDrives(testFile);
            }

            @Override
            public void onPendingStop() {
                cancelCheckDrives();
            }
        });
        frame.getContentPane().add(controlPane, BorderLayout.PAGE_START);
    }

    private void checkDrives(@Nonnull File testFile) {
        cancelCheckDrives();
        List<DrivePane> drivePanes = drivesPane.getDrives();

        for (DrivePane drivePane : drivePanes) {
            drivePane.setCheckStatus(DrivePane.CheckStatus.NONE);
        }

        List<DrivePane> selectedDrivePanes = drivePanes.stream()
                .filter(DrivePane::isSelected)
                .collect(Collectors.toList());

        if (selectedDrivePanes.size() == 0) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Please select the drives to check");
            controlPane.stop();
            return;
        }

        drivesCheckWorker = new DrivesCheckWorker(selectedDrivePanes, testFile, controlPane);
        drivesCheckWorker.execute();
    }

    private void cancelCheckDrives() {
        if (drivesCheckWorker == null) {
            return;
        }
        drivesCheckWorker.cancel(true);
    }

    public static void main( String[] args ) throws Throwable {
        new App();
    }
}
