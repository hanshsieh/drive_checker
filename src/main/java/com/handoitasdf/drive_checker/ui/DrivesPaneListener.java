package com.handoitasdf.drive_checker.ui;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Created by icand on 2017/9/3.
 */
public interface DrivesPaneListener {
    void onDriveSelectionChanged(@Nonnull File drive, boolean selected);
}
