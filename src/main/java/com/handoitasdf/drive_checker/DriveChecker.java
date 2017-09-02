package com.handoitasdf.drive_checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CancellationException;

/**
 * Created by icand on 2017/8/30.
 */
public class DriveChecker {
    private final Logger LOGGER = LoggerFactory.getLogger(DriveChecker.class);
    private final File drive;
    private final File testFile;
    private final MessageDigestProvider digestProvider;
    private volatile FileTransferrer transferrer;
    private volatile FileChecker fileChecker;
    private File outputFile;
    private volatile boolean canceled = false;
    public DriveChecker(@Nonnull File drive, @Nonnull File testFile) {
        this.drive = drive;
        this.testFile = testFile;
        this.digestProvider = new MessageDigestProvider();
    }

    public void check() throws IOException, InterruptedException, CancellationException {
        try {
            prepare();
            if (canceled) {
                throw new CancellationException("Checking is canceled");
            }
            transferrer.transfer();

            if (canceled) {
                throw new CancellationException("Checking is canceled");
            }
            if (!fileChecker.check(transferrer.getDigest())) {
                throw new IOException("MD5 digest checking fails");
            }

        } finally {
            release();
        }
    }

    private synchronized void prepare() throws IOException {
        outputFile = getTargetFile();
        LOGGER.debug("Output file: " + outputFile.getPath());
        transferrer = new FileTransferrer(
                testFile, outputFile, digestProvider);
        fileChecker = new FileChecker(outputFile, digestProvider);
    }

    private void release() throws IOException {
        if (outputFile == null || !outputFile.exists()) {
            return;
        }
        if (!outputFile.delete()) {
            throw new IOException("Check succeeds, but fail to delete test file " + outputFile.getPath());
        }
        outputFile = null;
    }

    public synchronized void cancel() {
        canceled = true;
        if (transferrer != null) {
            transferrer.cancel();
        }
        if (fileChecker != null) {
            fileChecker.cancel();
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
