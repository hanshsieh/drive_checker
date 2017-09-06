package com.handoitasdf.drive_checker.ui;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by icand on 2017/9/6.
 */
public class DriveCheckReportExporter implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriveCheckReportExporter.class);
    private static final String DEFAULT_REPOTRT_PATH = "report.txt";
    private final String report;

    public DriveCheckReportExporter(@Nonnull String report) {
        this.report = report;
    }

    @Override
    public void run() {
        try {
            FileUtils.writeStringToFile(
                    new File(DEFAULT_REPOTRT_PATH),
                    report,
                    StandardCharsets.UTF_8);
        } catch (Exception ex) {
            LOGGER.error("Fail to write report to file {}", DEFAULT_REPOTRT_PATH, ex);
        }
    }
}
