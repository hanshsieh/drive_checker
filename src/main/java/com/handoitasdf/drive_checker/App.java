package com.handoitasdf.drive_checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import java.awt.*;
import java.io.File;

/**
 * Application entry point.
 */
public class App {
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private JFrame frame;

    public App() {
        initFrame();
    }

    private void initFrame() {
        frame = new JFrame("Drive Checker");
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

        JPanel drivesPanel = new JPanel();
        contentPane.add(drivesPanel, BorderLayout.CENTER);

        initDrivesPane(drivesPanel);
    }

    private void initDrivesPane(@Nonnull JPanel drivesPanel) {
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEFT);
        drivesPanel.setLayout(layout);
        drivesPanel.add(new JButton("1"));
        drivesPanel.add(new JButton("2"));
        drivesPanel.add(new JButton("3"));
        drivesPanel.add(new JButton("4"));
        drivesPanel.add(new JButton("5"));
        drivesPanel.add(new JButton("6"));
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
