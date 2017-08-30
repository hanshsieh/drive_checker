package com.handoitasdf.drive_checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * Application entry point.
 */
public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    public static void main( String[] args ) throws Throwable {
        File[] drives;
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
        }
    }
}
