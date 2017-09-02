package com.handoitasdf.drive_checker.ui;

import com.handoitasdf.drive_checker.DriveChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.SwingWorker;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by icand on 2017/9/2.
 */
public class DrivesCheckWorker extends SwingWorker<Void, Runnable> {

    private final static Logger LOGGER = LoggerFactory.getLogger(DrivesCheckWorker.class);
    private final static long WAIT_TIMEOUT = 1000 * 60;

    private final List<DrivePane> drivePanes;
    private final List<DriveChecker> driveCheckers = new ArrayList<>();
    private final List<Future> checkerFutures = new ArrayList<>();
    private final File testFile;
    private final int iterationCount;
    private DrivesCheckListener listener;
    private Instant startTime;
    private Instant doneTime;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public DrivesCheckWorker(
            @Nonnull List<DrivePane> drivePanes,
            @Nonnull File testFile,
            int iterationCount) {
        this.drivePanes = drivePanes;
        this.testFile = testFile;
        this.iterationCount = iterationCount;
        addPropertyChangeListener(event -> {
            String propName = event.getPropertyName();
            if (!"state".equals(propName)) {
                return;
            }
            StateValue state = (StateValue) event.getNewValue();
            if (state.equals(StateValue.STARTED)) {
                onWorkerStarted();
            }
        });
    }

    public void setListener(@Nonnull DrivesCheckListener listener) {
        this.listener = listener;
    }

    @Nonnull
    public List<DriveChecker> getCheckers() {
        return Collections.unmodifiableList(driveCheckers);
    }

    @Nullable
    public Instant getStartTime() {
        return startTime;
    }

    @Nullable
    public Instant getDoneTime() {
        return doneTime;
    }

    private void invokeListenerOnStart() {
        if (listener != null) {
            listener.onStart();
        }
    }

    private void invokeListenerOnStop() {
        if (listener != null) {
            listener.onStop();
        }
    }

    private void onWorkerStarted() {
        for (DrivePane drivePane : drivePanes) {
            drivePane.setCheckStatus(DrivePane.CheckStatus.RUNNING);
        }
        startTime = Instant.now();
        invokeListenerOnStart();
    }

    /**
     * Notice that when this swing worker is canceled, this method will be called immediately, even if
     * the background thread is still running. Therefore, in this method we create another thread to wait for
     * the background thread to finish.
     */
    @Override
    protected void done() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                LOGGER.debug("Canceling drive checkers");
                for (DriveChecker driveChecker : driveCheckers) {
                    driveChecker.cancel();
                }
                synchronized (checkerFutures) {
                    for (Future future : checkerFutures) {

                        // Notice that once we have called "cancel", calling "get" on the future just
                        // directly get CancellationException.
                        future.cancel(true);
                    }
                }
                executor.shutdown();
                executor.awaitTermination(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
                return null;
            }

            @Override
            protected void done() {
                doneTime = Instant.now();
                invokeListenerOnStop();
            }
        }.execute();

    }

    private synchronized void init() {
        driveCheckers.clear();
        for (DrivePane drivePane : drivePanes) {
            File drive = drivePane.getDrive();
            DriveChecker driveChecker = new DriveChecker(
                    drive,
                    testFile);
            driveCheckers.add(driveChecker);
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            init();
            for (int i = 0; i < drivePanes.size(); ++i) {
                synchronized (checkerFutures) {
                    if (isCancelled()) {
                        break;
                    }
                    DrivePane drivePane = drivePanes.get(i);
                    DriveChecker driveChecker = driveCheckers.get(i);
                    Future checkerFuture = executor.submit(new Worker(driveChecker, drivePane));
                    checkerFutures.add(checkerFuture);
                }
            }
            return null;
        } finally {
            executor.shutdown();
            executor.awaitTermination(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void process(List<Runnable> runnables) {
        for (Runnable runnable : runnables) {
            runnable.run();
        }
    }

    private class Worker implements Runnable {

        private final DriveChecker driveChecker;
        private final DrivePane drivePane;

        public Worker(@Nonnull DriveChecker driveChecker, @Nonnull DrivePane drivePane) {
            this.driveChecker = driveChecker;
            this.drivePane = drivePane;
        }

        @Override
        public void run() {
            File drive = drivePane.getDrive();
            LOGGER.info("Running check for drive {} for {} times", drive.getPath(), iterationCount);
            try {
                driveChecker.check(iterationCount);
                publish(() -> drivePane.setCheckStatus(DrivePane.CheckStatus.SUCCESS));
                LOGGER.info("Drive {} checking passed!", drive.getPath());
            } catch (InterruptedException | CancellationException ex) {
                publish(() -> drivePane.setCheckStatus(DrivePane.CheckStatus.CANCELED));
                LOGGER.info("Checking of drive {} is canceled", drive.getPath(), ex);
            } catch (Exception ex) {
                publish(() -> drivePane.setCheckStatus(DrivePane.CheckStatus.FAILED));
                LOGGER.error("Fail to check drive {}", drive.getPath(), ex);
            }
        }
    }
}
