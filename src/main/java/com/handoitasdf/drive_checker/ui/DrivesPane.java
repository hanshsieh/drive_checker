package com.handoitasdf.drive_checker.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileSystemView;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by icand on 2017/9/1.
 */
public class DrivesPane extends JPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrivesPane.class);
    private final List<DrivePane> drivePanes = new ArrayList<>();

    public DrivesPane() {
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEFT);
        setLayout(layout);
        refresh();
    }

    public void refresh() {

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                refreshNow();
                return null;
            }
        }.execute();

    }

    @Nonnull
    public List<DrivePane> getDrives() {
        return Collections.unmodifiableList(drivePanes);
    }

    private synchronized void refreshNow() {
        File[] drives = File.listRoots();

        FileSystemView fileSystemView = FileSystemView.getFileSystemView();

        drivePanes.clear();
        removeAll();
        for(File drive : drives) {
            if (!fileSystemView.isDrive(drive)) {
                LOGGER.debug("Root {} isn't drive, skip it", drive);
            }
            if (!drive.canWrite()) {
                LOGGER.debug("Drive {} isn't writable, skip it", drive);
                continue;
            }
            DrivePane drivePane = new DrivePane(drive);
            add(drivePane);
            drivePanes.add(drivePane);
        }
    }
}
