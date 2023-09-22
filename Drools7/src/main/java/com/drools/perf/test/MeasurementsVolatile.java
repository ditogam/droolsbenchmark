package com.drools.perf.test;

public class MeasurementsVolatile {
	private volatile long min;
	private volatile long max;
	private volatile long total;
	private volatile long count;
	private volatile long timeouts;

	private long timeoutThreshold;

	public MeasurementsVolatile(long timeoutThreshold) {
		this.timeoutThreshold = timeoutThreshold;
	}

	public synchronized void reset() {
		min = Long.MAX_VALUE;
		max = 0;
		total = 0;
		count = 0;
		timeouts = 0;
	}

	public long startMeasuring() {
		return System.currentTimeMillis();
	}

	public long finishMeasuring(long start) {
		return measure(System.currentTimeMillis() - start);
	}

	public long measure(long measurement) {
		synchronized (this) {
			min = Math.min(min, measurement);
			max = Math.max(max, measurement);
			total += measurement;
			count++;

			if (measurement > timeoutThreshold)
				timeouts++;
		}
		return measurement;
	}

	public long getMin() {
		if (min != Long.MAX_VALUE)
			return min;
		else
			return 0;
	}

	public long getMax() {
		return max;
	}

	public long getTotal() {
		return total;
	}

	public long getCount() {
		return count;
	}

	public long getTimeouts() {
		return timeouts;
	}

	public synchronized long getAvg() {
		if (count > 0)
			return total / count;
		else
			return 0;
	}

	public String toString() {

		StringBuilder s = new StringBuilder("{");
		s.append("min: ").append(min).append(", ");
		s.append("max: ").append(max).append(", ");
		s.append("avg: ").append(getAvg()).append(", ");
		s.append("count: ").append(count).append("}");
		return s.toString();
	}
}
