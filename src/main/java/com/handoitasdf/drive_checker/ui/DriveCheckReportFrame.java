package com.handoitasdf.drive_checker.ui;

import com.handoitasdf.drive_checker.DriveChecker;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import java.awt.Container;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by icand on 2017/9/2.
 */
public class DriveCheckReportFrame extends JFrame {

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    private final JTextArea reportTextArea = new JTextArea();
    private DrivesCheckWorker drivesChecker = null;

    public DriveCheckReportFrame() {
        super("Report");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        initContentPane();
    }

    private void initContentPane() {
        Container contentPane = getContentPane();
        contentPane.add(reportTextArea);
    }

    public void setDriveCheckers(@Nonnull DrivesCheckWorker drivesChecker) {
        this.drivesChecker = drivesChecker;
        reportTextArea.setText(generateReport());
    }

    @Nonnull
    private String generateReport() {
        StringBuilder builder = new StringBuilder();

        Instant startTime = drivesChecker.getStartTime();
        Instant doneTime = drivesChecker.getDoneTime();
        Validate.notNull(startTime);
        Validate.notNull(doneTime);
        builder.append("Start time: ")
                .append(formatInstant(startTime))
                .append("\nDone time: ")
                .append(formatInstant(doneTime))
                .append("\nElapsed time: ")
                .append(formatElapsedTime(startTime, doneTime))
                .append('\n');
        for (DriveChecker checker : drivesChecker.getCheckers()) {
            builder.append(generateDriveReport(checker));
        }
        return builder.toString();
    }

    private String generateDriveReport(@Nonnull DriveChecker driveChecker) {
        StringBuilder builder = new StringBuilder();
        Instant startTime = driveChecker.getStartTime();
        Instant doneTime = driveChecker.getDoneTime();
        Validate.notNull(startTime);
        Validate.notNull(doneTime);
        builder.append('\n')
                .append("# Drive ")
                .append(driveChecker.getDrive().getPath())
                .append("\nStart time: ")
                .append(formatInstant(startTime))
                .append("\nDone time: ")
                .append(formatInstant(doneTime))
                .append("\nElapsed time: ")
                .append(formatElapsedTime(startTime, doneTime))
                .append("\nSuccess count: ")
                .append(driveChecker.getCheckedCount())
                .append("\nResult: ");
        switch (driveChecker.getStatus()) {
            case SUCCESS:
                builder.append("Success");
                break;
            case CANCELED:
                builder.append("Canceled");
                break;
            default:
                builder.append("Failed (Reason: ")
                        .append(driveChecker.getFailedReason())
                        .append(')');
                break;
        }
        builder.append('\n');
        return builder.toString();
    }

    @Nonnull
    private String formatInstant(@Nonnull Instant instant) {
        return ZonedDateTime.ofInstant(
                instant,
                ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private static String formatElapsedTime(@Nonnull Instant start, @Nonnull Instant end) {
        long elapsedMillis = end.toEpochMilli() - start.toEpochMilli();
        long substracted = 0;
        long hr = TimeUnit.MILLISECONDS.toHours(elapsedMillis);
        substracted += TimeUnit.HOURS.toMillis(hr);
        long min = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis - substracted);
        substracted += TimeUnit.MINUTES.toMillis(min);
        long sec = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis - substracted);
        substracted += TimeUnit.SECONDS.toMillis(sec);
        long ms = TimeUnit.MILLISECONDS.toMillis(elapsedMillis - substracted);
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
    }
}
