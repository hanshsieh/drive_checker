package com.handoitasdf.drive_checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;

/**
 * Created by icand on 2017/8/30.
 */
public class DriveChecker {
    private final Logger LOGGER = LoggerFactory.getLogger(DriveChecker.class);
    private final File drive;
    private final File testFile;
    private final MessageDigestProvider digestProvider;
    public DriveChecker(@Nonnull File drive, @Nonnull File testFile) {
        this.drive = drive;
        this.testFile = testFile;
        this.digestProvider = new MessageDigestProvider();
    }

    public void check() throws IOException {
        File outputFile = getTargetFile();
        LOGGER.debug("Output file: " + outputFile.getPath());

        FileTransferrer transferrer = new FileTransferrer(
                testFile, outputFile, digestProvider);
        FileChecker checker = new FileChecker(outputFile, digestProvider);

        transferrer.transfer();
        if (!checker.check(transferrer.getDigest())) {
            throw new IOException("MD5 digest checking fails");
        }
        if (!outputFile.delete()) {
            throw new IOException("Check succeeds, but fail to delete test file " + outputFile.getPath());
        }
    }

    @Nonnull
    private File getTargetFile() throws IOException {
        File target = new File(drive, testFile.getName());
        if (testFile.equals(target)) {
            throw new IOException("Input file " + testFile.getPath() + " is the same as target file");
        }
        return target;
    }
}
