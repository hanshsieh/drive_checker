package com.handoitasdf.drive_checker;

/**
 * Created by icand on 2017/9/3.
 */
public interface DriveCheckerListener {
    void onDataCopied(int iteration, long copiedBytes);
}
