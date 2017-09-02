package com.handoitasdf.drive_checker.ui;

import sun.awt.shell.ShellFolder;

import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import java.awt.Image;
import java.io.File;
import java.util.List;

/**
 * Ref:
 * https://stackoverflow.com/questions/39918487/get-a-specific-version-of-the-icon-of-a-file-in-java
 * https://stackoverflow.com/questions/6714045/how-to-resize-jlabel-imageicon
 * https://stackoverflow.com/questions/1498506/java-how-to-get-the-thumbnail-from-a-file
 */
public class DriveLabel extends JLabel {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;
    private final File drive;
    public DriveLabel(@Nonnull File drive) {
        this.drive = drive;
        setText(drive.getPath());
        setHorizontalTextPosition(JLabel.CENTER);
        setVerticalTextPosition(JLabel.BOTTOM);
        initDriveIcon();
    }

    private void initDriveIcon() {
        new SwingWorker<Void, Icon>() {
            @Override
            protected Void doInBackground() throws Exception {

                // Notice that ShellFolder class only exists if you are using Sun JDK.
                Image image = ShellFolder.getShellFolder(drive).getIcon(true);
                setIcon(new ImageIcon(image.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH)));
                return null;
            }

            protected void process(List<Icon> chunks) {
                if (chunks.isEmpty()) {
                    return;
                }
                setIcon(chunks.get(0));
            }
        }.execute();
    }
}
