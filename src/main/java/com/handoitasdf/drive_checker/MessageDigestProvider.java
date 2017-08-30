package com.handoitasdf.drive_checker;

import javax.annotation.Nonnull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by icand on 2017/8/31.
 */
public class MessageDigestProvider {
    @Nonnull
    public MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5");
    }
}
