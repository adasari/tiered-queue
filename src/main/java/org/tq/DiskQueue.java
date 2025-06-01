package org.tq;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class DiskQueue {
    private final File dir;
    private final Deque<Segment> segments = new ArrayDeque<>();
    private int segmentIndex = 0;

    public DiskQueue(String dirPath) throws IOException {
        this.dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // load existing segments if any ? not needed for now.
        if (segments.isEmpty()) {
            segments.add(createNewSegment());
        }
    }

    private Segment createNewSegment() throws IOException {
        String name = String.format("segment_%05d.seg", segmentIndex++);
        File file = new File(dir, name);
        return new Segment(file);
    }

    public synchronized void append(String msg) throws IOException {
        byte[] data = encodeMessage(msg);

        Segment last = segments.peekLast();
        if (last == null || !last.canWrite(data.length)) {
            last = createNewSegment();
            segments.addLast(last);
        }

        last.append(data);
    }

    public synchronized List<String> loadNextBatch(int maxCount) {
        List<String> batch = new ArrayList<>();
        while (batch.size() < maxCount && !segments.isEmpty()) {
            Segment first = segments.peekFirst();
            byte[] msgBytes = first.readNextMessage();
            if (msgBytes == null) {
                // segment fully read, remove it
                try {
                    first.close();
                    first.getFile().delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                segments.pollFirst();
                continue;
            }
            batch.add(new String(msgBytes));
        }
        return batch;
    }

    public synchronized boolean hasUnreadData() {
        return !segments.isEmpty() && !segments.peekFirst().isEmpty();
    }

    public synchronized void close() throws IOException {
        for (Segment segment : segments) {
            segment.close();
        }
    }

    private byte[] encodeMessage(String msg) {
        byte[] msgBytes = msg.getBytes();
        ByteBuffer buf = ByteBuffer.allocate(4 + msgBytes.length);
        buf.putInt(msgBytes.length);
        buf.put(msgBytes);
        return buf.array();
    }
}

