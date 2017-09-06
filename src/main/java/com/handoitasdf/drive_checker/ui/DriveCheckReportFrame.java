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
import java.util.concurrent.TimeUnit;

/**
 * Created by icand on 2017/9/2.
 */
public class DriveCheckReportFrame extends JFrame {

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    private final JTextArea reportTextArea = new JTextArea();
    private final String report;

    public DriveCheckReportFrame(@Nonnull String report) {
        super("Report");
        this.report = report;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        initContentPane();
    }

    private void initContentPane() {
        Container contentPane = getContentPane();
        reportTextArea.setText(report);
        contentPane.add(reportTextArea);
    }
}
