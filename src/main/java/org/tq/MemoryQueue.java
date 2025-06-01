package org.tq;

import java.util.LinkedList;
import java.util.List;

public class MemoryQueue {
    private final LinkedList<String> queue = new LinkedList<>();
    private final int maxSize;

    public MemoryQueue(int maxSize) {
        this.maxSize = maxSize;
    }

    public synchronized boolean offer(String msg) {
        if (queue.size() >= maxSize) return false;
        queue.addLast(msg);
        return true;
    }

    public synchronized String poll() {
        return queue.pollFirst();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized boolean isLow() {
        return queue.size() < (maxSize / 2);
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized void refillFrom(List<String> messages) {
        for (String msg : messages) {
            if (queue.size() >= maxSize) break;
            queue.addLast(msg);
        }
    }
}

