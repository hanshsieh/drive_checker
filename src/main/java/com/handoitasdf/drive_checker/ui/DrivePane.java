package com.handoitasdf.drive_checker.ui;

import javax.annotation.Nonnull;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * Created by icand on 2017/9/1.
 */
public class DrivePane extends JPanel {
    private final File drive;
    public DrivePane(@Nonnull File drive) {
        this.drive = drive;
        add(new DriveLabel(drive));
    }


}
