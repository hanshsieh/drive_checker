package com.handoitasdf.drive_checker.ui;

import com.handoitasdf.drive_checker.CheckingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Point;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application entry point.
 */
public class App {
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

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

        // Put the frame at the center
        frame.setLocationRelativeTo(null);
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
            public void onPendingStart() {
                checkDrives();
            }

            @Override
            public void onPendingStop() {
                cancelDrivesChecking();
            }
        });
        frame.getContentPane().add(controlPane, BorderLayout.PAGE_START);
    }

    private void checkDrives() {
        cancelDrivesChecking();
        List<DrivePane> drivePanes = drivesPane.getDrives();

        for (DrivePane drivePane : drivePanes) {
            drivePane.setCheckStatus(CheckingStatus.PENDING);
        }

        List<File> selectedDrives = drivePanes.stream()
                .filter(DrivePane::isSelected)
                .map(DrivePane::getDrive)
                .collect(Collectors.toList());

        if (selectedDrives.size() == 0) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Please select the drives to check");
            controlPane.stop();
            return;
        }

        drivesCheckWorker = new DrivesCheckWorker(
                selectedDrives,
                controlPane.getTestFile(),
                controlPane.getIterationCount());
        drivesCheckWorker.setListener(new DrivesCheckListener() {
            @Override
            public void onStart() {
                controlPane.start();
                drivesPane.setEnabled(false);
            }

            @Override
            public void onStop() {
                controlPane.stop();
                drivesPane.setEnabled(true);
                showReportFrame();
            }

            @Override
            public void onDriveStatusChanged(@Nonnull File drive, @Nonnull CheckingStatus checkStatus) {
                for (DrivePane drivePane : drivePanes) {
                    if (drivePane.getDrive().equals(drive)) {
                        drivePane.setCheckStatus(checkStatus);
                        break;
                    }
                }
            }
        });
        drivesCheckWorker.execute();
    }

    private void showReportFrame() {
        DriveCheckReportFrame reportFrame = new DriveCheckReportFrame();
        reportFrame.setDriveCheckers(drivesCheckWorker);
        Point frameLocation = frame.getLocation();
        reportFrame.setLocation(frameLocation.x + 20, frameLocation.y + 20);
        reportFrame.setVisible(true);
    }

    private void cancelDrivesChecking() {
        if (drivesCheckWorker == null) {
            return;
        }
        drivesCheckWorker.cancel(true);
    }

    public static void main( String[] args ) throws Throwable {
        new App();
    }
}
