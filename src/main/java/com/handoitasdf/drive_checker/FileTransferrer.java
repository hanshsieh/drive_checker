package com.handoitasdf.drive_checker;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CancellationException;

/**
 * Created by icand on 2017/8/30.
 */
public class FileTransferrer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferrer.class);
    private static final int CHUNK_SIZE = 1024 * 1024;
    private MessageDigest digester;
    private byte[] digest = new byte[0];
    private final File outputFile;
    private final File inputFile;
    private final MessageDigestProvider digestProvider;
    private long inputSize;
    private long inputPosition = 0;
    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private byte[] buffer;
    private int bufferSize;
    private int bufferOffset;
    private volatile boolean canceled = false;
    public FileTransferrer(@Nonnull File inputFile,
                           @Nonnull File outputFile,
                           @Nonnull MessageDigestProvider digestProvider) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.digestProvider = digestProvider;
    }

    public void transfer() throws IOException, InterruptedException, CancellationException {
        try {
            initStreams();

            do {
                if (canceled) {
                    throw new CancellationException("Transferring is canceled");
                }
                LOGGER.debug("Total number bytes to transferred: {}, number of bytes transferred: {}",
                        inputSize, inputPosition);
            } while (transferChunk());

        } finally {
            release();
        }
    }

    public void cancel() {
        canceled = true;
    }

    @Nonnull
    public byte[] getDigest() {
        return digest;
    }

    private boolean transferChunk() throws IOException {
        if (!readChunk()) {
            return false;
        }
        writeChunk();
        return true;
    }


    private boolean readChunk() throws IOException {
        bufferOffset = 0;
        bufferSize = inputStream.read(buffer);
        if (bufferSize < 0) {
            digest = digester.digest();
            return false;
        } else {
            digester.update(buffer, bufferOffset, bufferSize);
            inputPosition += bufferSize;
            return true;
        }
    }

    private void writeChunk() throws IOException {
        outputStream.write(buffer, bufferOffset, bufferSize - bufferOffset);
    }

    private void initStreams() throws IOException {
        buffer = new byte[CHUNK_SIZE];
        inputPosition = 0;
        outputStream = null;
        inputStream = null;
        outputStream = new FileOutputStream(outputFile, false);
        inputStream = new FileInputStream(inputFile);
        inputSize = inputFile.length();
        try {
            digester = digestProvider.getMessageDigest();
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException("Fail to instantiate MD5 instance");
        }
    }

    private void release() {
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
        inputStream = null;
        outputStream = null;
        buffer = null;
    }
}
