package com.handoitasdf.drive_checker.ui;

import com.handoitasdf.drive_checker.DriveChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileSystemView;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Application entry point.
 */
public class App {
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private final JFrame frame = new JFrame("Drive Checker");
    private final List<File> selectedDrives = new ArrayList<>();
    private final ControlPane controlPane = new ControlPane(frame.getContentPane());

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

        contentPane.add(createDrivesPane(), BorderLayout.CENTER);
    }

    private void initControlPane() {
        controlPane.setControlPaneListener(testFile -> {
            for (File drive : selectedDrives) {
                DriveChecker driveChecker = new DriveChecker(
                        drive,
                        testFile);
                try {
                    driveChecker.check();
                    LOGGER.info("Drive " + drive.getPath() + " checking passed!");
                } catch (Exception ex) {
                    LOGGER.error("Fail to check drive " + drive.getPath());
                }
            }
        });
        frame.getContentPane().add(controlPane, BorderLayout.PAGE_START);
    }

    @Nonnull
    private JComponent createDrivesPane() {
        JPanel drivesPanel = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEFT);
        drivesPanel.setLayout(layout);

        // returns pathnames for files and directory
        File[] drives = File.listRoots();

        FileSystemView fileSystemView = FileSystemView.getFileSystemView();

        for(File drive : drives) {
            try {
                if (!fileSystemView.isDrive(drive)) {
                    LOGGER.debug("Root {} isn't drive, skip it", drive);
                }
                if (!drive.canWrite()) {
                    LOGGER.debug("Drive {} isn't writable, skip it", drive);
                    continue;
                }
                drivesPanel.add(new DrivePane(drive));
                selectedDrives.add(drive);
            } catch (Exception ex) {
                LOGGER.error("Fail to check drive: {}", drive.getPath(), ex);
            }
        }

        return drivesPanel;

        /*DriveChecker driveChecker = new DriveChecker(
                        drive,
                        new File("D:\\Users\\someone\\Downloads\\picpick_inst.exe"));
                driveChecker.check();
                LOGGER.info("Drive " + drive.getPath() + " checking passed!");*/
    }

    public static void main( String[] args ) throws Throwable {
        new App();
        /*File[] drives;
        FileSystemView fsv = FileSystemView.getFileSystemView();

        // returns pathnames for files and directory
        drives = File.listRoots();

        FileSystemView fileSystemView = FileSystemView.getFileSystemView();

        for(File drive : drives) {
            try {
                LOGGER.info("=== Check drive {} ===", drive.getPath());
                if (!fileSystemView.isDrive(drive)) {
                    LOGGER.debug("Root {} isn't drive, skip it", drive);
                }
                if (!drive.canWrite()) {
                    LOGGER.debug("Drive {} isn't writable, skip it", drive);
                    continue;
                }
                DriveChecker driveChecker = new DriveChecker(
                        drive,
                        new File("D:\\Users\\someone\\Downloads\\picpick_inst.exe"));
                driveChecker.check();
                LOGGER.info("Drive " + drive.getPath() + " checking passed!");
            } catch (Exception ex) {
                LOGGER.error("Fail to check drive: {}", drive.getPath(), ex);
            }
        }*/
    }
}
