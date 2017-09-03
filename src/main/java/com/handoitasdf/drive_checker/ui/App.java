package com.handoitasdf.drive_checker.ui;

import com.handoitasdf.drive_checker.CheckingStatus;
import com.handoitasdf.drive_checker.DriveChecker;
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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Application entry point.
 */
public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final String PROP_TEST_COUNT = "test.repeat_count";
    private static final String PROP_TEST_FILE_PATH = "test.input_file_path";
    private static final String PROP_DRIVE_SELECTED_FORMAT = "drives.%s.selected";
    private static final Pattern PROP_DRIVE_SELECTED_PATTERN = Pattern.compile("drives.([^.]*).selected");
    private static final String PROPERTY_FILE_PATH = "user.properties";
    private final JFrame frame = new JFrame("Drive Checker");
    private final DrivesPane drivesPane = new DrivesPane();
    private final ControlPane controlPane = new ControlPane(frame.getContentPane());
    private DrivesCheckWorker drivesCheckWorker = null;
    private final PropertiesProvider propertiesProvider;

    public App() {
        propertiesProvider = new PropertiesProvider(new File(PROPERTY_FILE_PATH));
        initFrame();
        drivesPane.refresh();
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
        initDrivesPane();
    }

    private void initDrivesPane() {

        drivesPane.setListener(new DrivesPaneListener() {
            @Override
            public void onDriveSelectionChanged(@Nonnull File drive, boolean selected) {
                propertiesProvider.setProperty(
                        String.format(PROP_DRIVE_SELECTED_FORMAT, drive.getPath()),
                        String.valueOf(selected));
            }

            @Override
            public void onRefreshDone() {
                LOGGER.debug("DrivesPane refresh done");
                refreshDrivesPaneProperties();
            }
        });

        Container contentPane = frame.getContentPane();
        contentPane.add(drivesPane, BorderLayout.CENTER);
    }

    private void refreshDrivesPaneProperties() {
        propertiesProvider.getProperties().forEach((propKey, value) -> {
            Matcher matcher = PROP_DRIVE_SELECTED_PATTERN.matcher(propKey);
            if (!matcher.matches()) {
                return;
            }
            boolean selected = Boolean.parseBoolean(value);
            String drive = matcher.group(1);
            drivesPane.getDrivePane(new File(drive)).ifPresent(drivePane -> drivePane.setSelected(selected));
        });
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

            @Override
            public void onTestFileChanged(@Nonnull File file) {
                propertiesProvider.setProperty(PROP_TEST_FILE_PATH, file.getAbsolutePath());
            }

            @Override
            public void onRepeatCountChanged(int newValue) {
                propertiesProvider.setProperty(PROP_TEST_COUNT, String.valueOf(newValue));
            }
        });
        initControlPaneProperties();
        frame.getContentPane().add(controlPane, BorderLayout.PAGE_START);
    }

    private void initControlPaneProperties() {
        propertiesProvider.getProperty(PROP_TEST_COUNT)
                .ifPresent(prop -> controlPane.setTestCount(Integer.parseInt(prop)));
        propertiesProvider.getProperty(PROP_TEST_FILE_PATH)
                .ifPresent(prop -> controlPane.setTestFile(new File(prop)));
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
                getDrivePaneByDrive(drive).ifPresent(d -> d.setCheckStatus(checkStatus));
            }
        });
        for (DriveChecker checker : drivesCheckWorker.getCheckers()) {
            checker.setListener((iteration, copiedBytes) -> {
                File drive = checker.getDrive();
                getDrivePaneByDrive(drive).ifPresent(d -> {
                    d.setCopiedSize(copiedBytes);
                    d.setIterationCount(iteration);
                });
            });
        }
        drivesCheckWorker.execute();
    }

    @Nonnull
    private Optional<DrivePane> getDrivePaneByDrive(@Nonnull File drive) {
        for (DrivePane drivePane : drivesPane.getDrives()) {
            if (drivePane.getDrive().equals(drive)) {
                return Optional.of(drivePane);
            }
        }
        return Optional.empty();
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
