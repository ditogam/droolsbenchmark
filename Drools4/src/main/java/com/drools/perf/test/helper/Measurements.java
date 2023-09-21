package com.drools.perf.test.helper;

public class Measurements {
    private final long timeoutThreshold;
    private volatile long min;
    private volatile long max;
    private volatile long total;
    private volatile long count;
    private volatile long timeouts;

    public Measurements(long timeoutThreshold) {
        this.timeoutThreshold = timeoutThreshold;

        reset();
    }

    public synchronized void reset() {
        min = Long.MAX_VALUE;
        max = 0;
        total = 0;
        count = 0;
        timeouts = 0;
    }

    public long log(long measurement) {
        min = Math.min(min, measurement);
        max = Math.max(max, measurement);
        total += measurement;
        count++;

//        avg.set((double) total.get() / (double) count.incrementAndGet());
//
//        if (measurement > timeoutThreshold) {
//            timeouts.incrementAndGet();
//        }

        return measurement;
    }

    private synchronized void total(long measurement) {
        total += measurement;
    }

    private synchronized void max(long measurement) {
        max = Math.max(max, measurement);
    }

    private synchronized void min(long measurement) {
        min = Math.min(min, measurement);
    }

    public long getMin() {
        long value = min;

        return value == Long.MAX_VALUE ? 0 : value;
    }

    public String toString() {
        return "{" + "min: " + min + ", "
            + "max: " + max + ", "
            + "count: " + count + "}";
    }
}

