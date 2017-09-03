package com.handoitasdf.drive_checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
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
    private Instant startTime;
    private Instant doneTime;
    private int finishedIteration = 0;
    private volatile CheckingStatus status = CheckingStatus.PENDING;
    private Exception failedReason;
    private DriveCheckerListener listener;
    public DriveChecker(@Nonnull File drive, @Nonnull File testFile) {
        this.drive = drive;
        this.testFile = testFile;
        this.digestProvider = new MessageDigestProvider();
    }

    public void check(int maxIterations) throws IOException, InterruptedException, CancellationException {
        if (!CheckingStatus.PENDING.equals(status)) {
            throw new IllegalStateException("This current status is " + status
                    + ", and checking cannot be run multiple times");
        }
        try {
            setStatusUnlessCanceled(CheckingStatus.RUNNING);
            prepare();
            while (true) {
                transferrer.transfer();
                setStatusUnlessCanceled(CheckingStatus.RUNNING);
                if (!fileChecker.check(transferrer.getDigest())) {
                    throw new IOException("MD5 digest checking fails");
                }
                ++finishedIteration;
                if (maxIterations > 0 && finishedIteration >= maxIterations) {
                    break;
                }
            }
            setStatusUnlessCanceled(CheckingStatus.SUCCESS);
        } catch (Exception ex) {
            setStatusUnlessCanceled(CheckingStatus.FAILED);
            failedReason = ex;
            throw ex;
        } finally {
            release();
        }
    }

    public void setListener(@Nullable DriveCheckerListener listener) {
        this.listener = listener;
    }

    private void invokeDataCopiedListener(int iteration, long dataCopied) {
        if (listener != null) {
            listener.onDataCopied(iteration, dataCopied);
        }
    }

    private synchronized void setStatusUnlessCanceled(@Nonnull CheckingStatus newStatus) {
        if (CheckingStatus.CANCELED.equals(status)) {
            throw new CancellationException(
                    "Cannot set status to " + newStatus + " because checking is canceled");
        }
        this.status = newStatus;
    }

    @Nullable
    public Exception getFailedReason() {
        return failedReason;
    }

    @Nullable
    public Instant getStartTime() {
        return startTime;
    }

    @Nullable
    public Instant getDoneTime() {
        return doneTime;
    }

    @Nonnull
    public File getDrive() {
        return drive;
    }

    @Nonnull
    public CheckingStatus getStatus() {
        return status;
    }

    public int getCheckedCount() {
        return finishedIteration;
    }

    private synchronized void prepare() throws IOException {
        startTime = Instant.now();
        doneTime = null;
        finishedIteration = 0;
        outputFile = getTargetFile();
        LOGGER.debug("Output file: {}", outputFile.getPath());
        transferrer = new FileTransferrer(
                testFile, outputFile, digestProvider);
        fileChecker = new FileChecker(outputFile, digestProvider);
        transferrer.setListener(numBytes -> {
            invokeDataCopiedListener(finishedIteration + 1, numBytes);
        });
    }

    private synchronized void release() throws IOException {
        doneTime = Instant.now();
        if (outputFile == null || !outputFile.exists()) {
            return;
        }
        if (!outputFile.delete()) {
            throw new IOException("Check succeeds, but fail to delete test file " + outputFile.getPath());
        }
        outputFile = null;
    }

    /**
     * Cancel the checker.
     *
     * @return True if the cancellation request has been sent;
     *         false if the checker has already been canceled or is done.
     */
    public synchronized boolean cancel() {
        if (!CheckingStatus.PENDING.equals(status) && !CheckingStatus.RUNNING.equals(status)) {
            return false;
        }
        status = CheckingStatus.CANCELED;
        if (transferrer != null) {
            transferrer.cancel();
        }
        if (fileChecker != null) {
            fileChecker.cancel();
        }
        return true;
    }

    @Nonnull
    private File getTargetFile() throws IOException {
        return File.createTempFile(testFile.getName() + ".", ".tmp", drive);
    }
}
