<h2>Tiered Queue:</h2>

TieredQueue is a high-performance, embedded hybrid (memory + file backed) message queue built for systems where:
* Low-latency reads are essential
* Durability is required
* Scalability beyond memory limits is needed

It combines an in-memory FIFO queue for fast access and a disk-backed write-ahead log (WAL) using memory-mapped segmented files for durability and capacity.
It behaves like a sliding pipe where the head lives in memory (hot path), and the tail is written to disk (cold path).

<h2>Features:</h2>
* Sliding memory window for fast, FIFO in-memory reads
* Disk-backed persistence using memory-mapped segment files
* Automatic backfill of memory from disk
* Zero-copy IO via MappedByteBuffer
* Strict FIFO ordering preserved across memory and disk
* Efficient resource usage (memory + disk)
* Embeddable: no external dependencies or brokers


<h2>Architecture:</h2>

```
        [ Memory Queue (Head) ] →→→→→ Readers
                ↑
Writers ---→    |  
                ↓
         [ Disk Queue (Tail) ]   
```

1. Writers:
* Prefer memory queue if disk has no backlog.
* Otherwise write to Disk Queue i.e disk log segments.
2. Memory Queue:
* Fixed-size FIFO buffer for low-latency reads.
3. Disk Queue:
* Segmented, memory-mapped files for durable storage.
* Loader reads from disk to refill memory when it runs low.

<h2>Future Improvements:</h2>
* Standardized API for easier integration with file based queues like Chronicle Queue.
* Concurrent access optimizations.
* Kryo serialization support.
* Metrics and monitoring.
