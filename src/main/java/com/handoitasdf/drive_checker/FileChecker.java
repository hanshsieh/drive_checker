package com.handoitasdf.drive_checker;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by icand on 2017/8/31.
 */
public class FileChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileChecker.class);
    private static final int CHUNK_SIZE = 1024 * 1024;
    private final File file;
    private long filePosition;
    private long fileSize;
    private FileInputStream inputStream;
    private byte[] buffer;
    private final MessageDigestProvider digestProvider;
    private MessageDigest digester;
    private byte[] realDigest;
    public FileChecker(@Nonnull File file, @Nonnull MessageDigestProvider digestProvider) {
        this.file = file;
        this.digestProvider = digestProvider;
    }

    public boolean check(byte[] digest) throws IOException {
        try {
            init();
            while (readChunk()) {
                LOGGER.debug("Total bytes: {}, current offset: {}", fileSize, filePosition);
            }
            return Arrays.equals(realDigest, digest);
        } finally {
            release();
        }
    }

    private boolean readChunk() throws IOException {
        int nRead = inputStream.read(buffer);
        if (nRead < 0) {
            realDigest = digester.digest();
            return false;
        }
        filePosition += nRead;
        digester.update(buffer, 0, nRead);
        return true;
    }

    private void init() throws IOException {
        inputStream = new FileInputStream(file);
        filePosition = 0;
        fileSize = file.length();
        buffer = new byte[CHUNK_SIZE];
        try {
            digester = digestProvider.getMessageDigest();
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException("Fail to instantiate message digest", ex);
        }
    }

    private void release() {
        IOUtils.closeQuietly(inputStream);
        inputStream = null;
    }
}
