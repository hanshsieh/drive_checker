package com.handoitasdf.drive_checker.ui;

import com.handoitasdf.drive_checker.DriveChecker;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Created by icand on 2017/9/6.
 */
public class DriveCheckReportGenerator {

    @Nonnull
    public String generateReport(@Nonnull DrivesCheckWorker drivesChecker) {
        StringBuilder builder = new StringBuilder();

        Instant startTime = drivesChecker.getStartTime();
        Instant doneTime = drivesChecker.getDoneTime();
        Validate.notNull(startTime);
        Validate.notNull(doneTime);
        builder.append("Start time: ")
                .append(formatInstant(startTime))
                .append(System.lineSeparator())
                .append("Done time: ")
                .append(formatInstant(doneTime))
                .append(System.lineSeparator())
                .append("Elapsed time: ")
                .append(formatElapsedTime(startTime, doneTime))
                .append(System.lineSeparator());
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
        builder.append(System.lineSeparator())
                .append("# Drive ")
                .append(driveChecker.getDrive().getPath())
                .append(System.lineSeparator())
                .append("Start time: ")
                .append(formatInstant(startTime))
                .append(System.lineSeparator())
                .append("Done time: ")
                .append(formatInstant(doneTime))
                .append(System.lineSeparator())
                .append("Elapsed time: ")
                .append(formatElapsedTime(startTime, doneTime))
                .append(System.lineSeparator())
                .append("Success count: ")
                .append(driveChecker.getCheckedCount())
                .append(System.lineSeparator())
                .append("Result: ");
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
        builder.append(System.lineSeparator());
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
