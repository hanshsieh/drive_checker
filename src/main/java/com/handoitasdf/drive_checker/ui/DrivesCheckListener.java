package com.handoitasdf.drive_checker.ui;

import com.handoitasdf.drive_checker.CheckingStatus;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Created by icand on 2017/9/2.
 */
public interface DrivesCheckListener {
    void onStart();
    void onStop();
    void onDriveStatusChanged(@Nonnull File drive, @Nonnull CheckingStatus checkStatus);
}
