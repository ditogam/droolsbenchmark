package com.drools.perf.test;

import java.util.concurrent.atomic.AtomicLong;

public class Measurements {
    private final long timeoutThreshold;
    private final AtomicLong min = new AtomicLong();
    private final AtomicLong max = new AtomicLong();
    private final AtomicLong total = new AtomicLong();
    private final AtomicLong count = new AtomicLong();
    private final AtomicLong timeouts = new AtomicLong();

    public Measurements(long timeoutThreshold) {
        this.timeoutThreshold = timeoutThreshold;

        reset();
    }

    public synchronized void reset() {
        min.set(Long.MAX_VALUE);
        max.set(0);
        total.set(0);
        count.set(0);
        timeouts.set(0);
        timeouts.set(0);
    }

    public long startMeasuring() {
        return System.currentTimeMillis();
    }

    public long finishMeasuring(long start) {
        return measureUpdateAndGet(System.currentTimeMillis() - start);
    }

    public long measureUpdateAndGet(long measurement) {
        min.updateAndGet(operand -> Math.min(operand, measurement));
        max.updateAndGet(operand -> Math.max(operand, measurement));
        total.addAndGet(measurement);
        count.incrementAndGet();

        if (measurement > timeoutThreshold) {
            timeouts.incrementAndGet();
        }

        return measurement;
    }

    public long measureGetSet(long measurement) {
        min.set(Math.min(min.get(), measurement));
        max.set(Math.max(max.get(), measurement));
        total.addAndGet(measurement);
        count.incrementAndGet();

        if (measurement > timeoutThreshold) {
            timeouts.incrementAndGet();
        }

        return measurement;
    }

    public long getMin() {
        long value = min.get();

        return value == Long.MAX_VALUE ? 0 : value;
    }

    public long getMax() {
        return max.get();
    }

    public long getTotal() {
        return total.get();
    }

    public long getCount() {
        return count.get();
    }

    public long getTimeouts() {
        return timeouts.get();
    }

    public double getAvg() {
        double currentCount = count.get();
        double currentTotal = total.get();

        return currentCount > 0 ? currentTotal / currentCount : 0;
    }

    public String toString() {
        return "{" + "min: " + min + ", "
            + "max: " + max + ", "
            + "avg: " + String.format("%,.4f", getAvg()) + ", "
            + "count: " + count + "}";
    }

    public static void main(String[] args) {
        Measurements m = new Measurements(10);
        for (long i = 0; i < m.timeoutThreshold; i++) {
            m.measureUpdateAndGet(i);
        }
        System.out.println(m);
    }
}
