package org.tq;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TieredQueue {
    private final MemoryQueue memoryQueue;
    private final DiskQueue diskLog;
    private final ScheduledExecutorService loader = Executors.newSingleThreadScheduledExecutor();

    public TieredQueue(String segmentPath, int memorySize) throws IOException {
        this.memoryQueue = new MemoryQueue(memorySize);
        this.diskLog = new DiskQueue(segmentPath);

        loader.scheduleAtFixedRate(this::loadFromDisk, 100, 100, TimeUnit.MILLISECONDS);
    }

    public synchronized void write(String msg) throws IOException {
        // If disk has unread data, must append to disk to preserve order
        if (diskLog.hasUnreadData()) {
            diskLog.append(msg);
            return;
        }

        // If there's room in memory queue, write there
        boolean inMem = memoryQueue.offer(msg);

        // If not, write to disk
        if (!inMem) {
            diskLog.append(msg);
        }
    }

    public synchronized String read() {
        String msg = memoryQueue.poll();
        if (msg == null && diskLog.hasUnreadData()) {
            List<String> batch = diskLog.loadNextBatch(100);
            memoryQueue.refillFrom(batch);
            msg = memoryQueue.poll();
        }
        return msg;
    }

    private void loadFromDisk() {
        if (memoryQueue.isLow()) {
            List<String> batch = diskLog.loadNextBatch(10);
            memoryQueue.refillFrom(batch);
        }
    }

    public void shutdown() throws IOException {
        loader.shutdown();
        diskLog.close();
    }
}
