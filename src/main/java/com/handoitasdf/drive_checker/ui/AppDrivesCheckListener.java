package com.handoitasdf.drive_checker.ui;

import com.handoitasdf.drive_checker.CheckingStatus;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.concurrent.Executor;

/**
 * Created by icand on 2017/9/6.
 */
class AppDrivesCheckListener implements DrivesCheckListener {

    private final ControlPane controlPane;
    private final DrivesPane drivesPane;
    private DrivesCheckWorker worker;
    private final Executor executor;
    private final DriveCheckReportGenerator reportGenerator = new DriveCheckReportGenerator();

    public AppDrivesCheckListener(
            @Nonnull ControlPane controlPane,
            @Nonnull DrivesPane drivesPane,
            @Nonnull DrivesCheckWorker worker,
            @Nonnull Executor executor) {
        this.controlPane = controlPane;
        this.drivesPane = drivesPane;
        this.worker = worker;
        this.executor = executor;
    }

    @Override
    public void onStart() {
        controlPane.start();
        drivesPane.setEnabled(false);
        controlPane.setReportEnabled(false);
    }

    @Override
    public void onStop() {
        controlPane.stop();
        drivesPane.setEnabled(true);
        String report = reportGenerator.generateReport(worker);
        executor.execute(new DriveCheckReportExporter(report));
        controlPane.setReport(report);
        controlPane.setReportEnabled(true);
    }

    @Override
    public void onDriveStatusChanged(@Nonnull File drive, @Nonnull CheckingStatus checkStatus) {
        drivesPane.getDrivePaneByDrive(drive).ifPresent(d -> d.setCheckStatus(checkStatus));
    }
}
