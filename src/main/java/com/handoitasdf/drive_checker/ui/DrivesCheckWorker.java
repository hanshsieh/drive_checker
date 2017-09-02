package com.handoitasdf.drive_checker.ui;

import com.handoitasdf.drive_checker.DriveChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.SwingWorker;
import java.io.File;
import java.util.ArrayList;
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
    private final ControlPane controlPane;

    public DrivesCheckWorker(
            @Nonnull List<DrivePane> drivePanes,
            @Nonnull ControlPane controlPane) {
        this.drivePanes = drivePanes;
        this.testFile = controlPane.getTestFile();
        this.iterationCount = controlPane.getIterationCount();
        this.controlPane = controlPane;
        addPropertyChangeListener(event -> {
            String propName = event.getPropertyName();
            if (!"state".equals(propName)) {
                return;
            }
            StateValue state = (StateValue) event.getNewValue();
            if (state.equals(StateValue.STARTED)) {
                onWorkerStarted();
            } else if (state.equals(StateValue.DONE)) {
                onWorkerDone();
            }
        });
    }

    private void onWorkerStarted() {
        for (DrivePane drivePane : drivePanes) {
            drivePane.setCheckStatus(DrivePane.CheckStatus.RUNNING);
        }
        controlPane.start();
    }

    private void onWorkerDone() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                LOGGER.debug("Canceling drive checkers");
                for (DriveChecker driveChecker : driveCheckers) {
                    driveChecker.cancel();
                }
                synchronized (checkerFutures) {
                    for (Future future : checkerFutures) {
                        future.cancel(true);
                        future.get();
                    }
                }
                LOGGER.debug("All drive checkers have been canceled");
                return null;
            }

            @Override
            protected void done() {
                controlPane.stop();
            }
        }.execute();

    }

    private synchronized void init() {
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
        init();
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
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
        } finally {
            executor.shutdown();
            executor.awaitTermination(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
            publish(controlPane::stop);
        }
        return null;
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
        private int iteration = 0;

        public Worker(@Nonnull DriveChecker driveChecker, @Nonnull DrivePane drivePane) {
            this.driveChecker = driveChecker;
            this.drivePane = drivePane;
        }

        @Override
        public void run() {
            File drive = drivePane.getDrive();
            iteration = 0;
            LOGGER.info("Running check for drive {} for {} times", drive.getPath(), iterationCount);
            try {
                while (hasMoreIteration()) {
                    LOGGER.info("Running iteration {} for drive {}", iteration, drive.getPath());
                    driveChecker.check();
                }
                publish(() -> drivePane.setCheckStatus(DrivePane.CheckStatus.SUCCESS));
                LOGGER.info("Drive {} checking passed!", drive.getPath());
            } catch (InterruptedException | CancellationException ex) {
                publish(() -> drivePane.setCheckStatus(DrivePane.CheckStatus.CANCELED));
                LOGGER.warn("Checking of drive {} is canceled", drive.getPath(), ex);
            } catch (Exception ex) {
                publish(() -> drivePane.setCheckStatus(DrivePane.CheckStatus.FAILED));
                LOGGER.error("Fail to check drive {}", drive.getPath(), ex);
            }
        }

        private boolean hasMoreIteration() {
            if (iterationCount <= 0) {
                return true;
            }
            return iteration++ < iterationCount;
        }
    }
}
