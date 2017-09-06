package com.handoitasdf.drive_checker.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileSystemView;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by icand on 2017/9/1.
 */
public class DrivesPane extends JPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrivesPane.class);
    private final List<DrivePane> drivePanes = new ArrayList<>();
    private final JPanel drivesPanel = new JPanel();
    private final JButton refreshBtn = new JButton("Refresh");
    private DrivesPaneListener listener;

    public DrivesPane() {
        initLayout();
    }

    public void setListener(@Nullable DrivesPaneListener listener) {
        this.listener = listener;
    }

    public void refresh() {

        refreshBtn.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                LOGGER.debug("Refreshing...");
                File[] drives = File.listRoots();

                FileSystemView fileSystemView = FileSystemView.getFileSystemView();

                synchronized (drivePanes) {
                    List<DrivePane> oldDrivePanes = new ArrayList<>(drivePanes);
                    clearDrivePaneListeners();
                    drivePanes.clear();
                    for (File drive : drives) {
                        if (!fileSystemView.isDrive(drive)) {
                            LOGGER.debug("Root {} isn't drive, skip it", drive);
                        }
                        if (!drive.canWrite()) {
                            LOGGER.debug("Drive {} isn't writable, skip it", drive);
                            continue;
                        }
                        DrivePane drivePane = createDrivePaneIfNotExists(drive, oldDrivePanes);
                        drivePanes.add(drivePane);
                        drivePane.setListener(new ReportDrivePaneListener(drivePane));
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                LOGGER.debug("Refresh finished");
                refreshBtn.setEnabled(true);
                drivesPanel.removeAll();
                for (DrivePane drivePane : drivePanes) {
                    drivesPanel.add(drivePane);
                }
                drivesPanel.revalidate();
                drivesPanel.repaint();
                if (listener != null) {
                    listener.onRefreshDone();
                }
            }
        }.execute();

    }

    private void clearDrivePaneListeners() {
        for (DrivePane drivePane : drivePanes) {
            drivePane.setListener(null);
        }
    }

    @Nonnull
    public List<DrivePane> getDrivePanes() {
        return Collections.unmodifiableList(drivePanes);
    }

    @Nonnull
    public Optional<DrivePane> getDrivePaneByDrive(@Nonnull File drive) {
        for (DrivePane drivePane : drivePanes) {
            if (drivePane.getDrive().equals(drive)) {
                return Optional.of(drivePane);
            }
        }
        return Optional.empty();
    }

    @Nonnull
    public Optional<DrivePane> getDrivePane(@Nonnull File drive) {
        for (DrivePane drivePane : drivePanes) {
            if (drivePane.getDrive().equals(drive)) {
                return Optional.of(drivePane);
            }
        }
        return Optional.empty();
    }

    private void initLayout() {
        setLayout(new GridBagLayout());
        initControlPane();
        initDrivesPanel();
    }

    private void initControlPane() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.anchor = GridBagConstraints.NORTHEAST;
        add(refreshBtn, constraints);

        refreshBtn.addActionListener(e -> refresh());
    }

    private void initDrivesPanel() {
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEFT);
        drivesPanel.setLayout(layout);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;

        add(drivesPanel, constraints);
    }

    @Nonnull
    private DrivePane createDrivePaneIfNotExists(
            @Nonnull File drive,
            @Nonnull Collection<DrivePane> oldDrivePanes) {
        for (DrivePane oldDrivePane : oldDrivePanes) {
            if (oldDrivePane.getDrive().equals(drive)) {
                return oldDrivePane;
            }
        }
        return new DrivePane(drive);
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (DrivePane drivePane : drivePanes) {
            drivePane.setEnabled(enabled);
        }
        refreshBtn.setEnabled(enabled);
    }

    private class ReportDrivePaneListener implements DrivePaneListener {

        private final DrivePane drivePane;

        public ReportDrivePaneListener(@Nonnull DrivePane drivePane) {
            this.drivePane = drivePane;
        }

        @Override
        public void onSelectionChanged(boolean selected) {
            if (listener != null) {
                listener.onDriveSelectionChanged(drivePane.getDrive(), selected);
            }
        }
    }
}
