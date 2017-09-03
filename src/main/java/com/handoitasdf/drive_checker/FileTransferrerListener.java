package com.handoitasdf.drive_checker;

/**
 * Created by icand on 2017/9/3.
 */
public interface FileTransferrerListener {
    void onDataTransferred(long numBytes);
}
