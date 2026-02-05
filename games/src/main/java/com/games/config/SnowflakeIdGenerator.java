package com.games.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class SnowflakeIdGenerator {

    private static final long EPOCH = 1700000000000L;

    private static final long MACHINE_ID_BITS = 10;
    private static final long SEQUENCE_BITS = 12;

    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private final long machineId;

    /**
     * 存 lastTimestamp << SEQUENCE_BITS | sequence
     */
    private final AtomicLong last = new AtomicLong(-1L);

    public SnowflakeIdGenerator(@Value("${server.machine-id:1}") long machineId) {
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException("Invalid machine id");
        }
        this.machineId = machineId;
    }

    public long nextId() {
        while (true) {
            long now = System.currentTimeMillis();
            long old = last.get();

            long lastTs = old >>> SEQUENCE_BITS;
            long seq = old & MAX_SEQUENCE;

            long newSeq;
            if (now == lastTs) {
                newSeq = seq + 1;
                if (newSeq > MAX_SEQUENCE) {
                    // sequence 滿了 → 等下一毫秒
                    while ((now = System.currentTimeMillis()) <= lastTs) ;
                    newSeq = 0;
                }
            } else {
                newSeq = 0;
            }

            long next = (now << SEQUENCE_BITS) | newSeq;

            if (last.compareAndSet(old, next)) {
                return ((now - EPOCH) << (MACHINE_ID_BITS + SEQUENCE_BITS))
                        | (machineId << SEQUENCE_BITS)
                        | newSeq;
            }
        }
    }
}

