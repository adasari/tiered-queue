package org.tq;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Segment {
    private static final int SEGMENT_SIZE = 64 * 1024 * 1024; // 64MB

    private final File file;
    private final RandomAccessFile raf;
    private final MappedByteBuffer buffer;
    private int writePosition = 0;
    private int readPosition = 0;

    public Segment(File file) throws IOException {
        this.file = file;
        this.raf = new RandomAccessFile(file, "rw");
        if (raf.length() < SEGMENT_SIZE) {
            raf.setLength(SEGMENT_SIZE);
        }
        this.buffer = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, SEGMENT_SIZE);
    }

    public boolean isEmpty() {
        return writePosition == 0;
    }

    public boolean canWrite(int length) {
        return writePosition + length <= SEGMENT_SIZE;
    }

    public boolean canRead() {
        return readPosition < writePosition;
    }

    public void append(byte[] data) {
        buffer.position(writePosition);
        buffer.put(data);
        writePosition += data.length;
    }

    public byte[] readNextMessage() {
        if (!canRead()) return null;

        buffer.position(readPosition);
        if (SEGMENT_SIZE - readPosition < 4) return null; // no room for length

        int length = buffer.getInt();
        if (length <= 0 || length > SEGMENT_SIZE) return null; // corrupt or incomplete

        if (readPosition + 4 + length > writePosition) return null; // incomplete

        byte[] msg = new byte[length];
        buffer.get(msg);
        readPosition += 4 + length;
        return msg;
    }

    public boolean isFullyRead() {
        return readPosition >= writePosition;
    }

    public void close() throws IOException {
        raf.close();
    }

    public File getFile() {
        return file;
    }
}

