package com.drools.perf.test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.AsyncProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static org.openjdk.jmh.annotations.Threads.MAX;

@Threads(MAX)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
//@BenchmarkMode(Mode.AverageTime)
//@OutputTimeUnit(TimeUnit.MILLISECONDS)

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(jvmArgs = {"-Xms30G",
    "-Xmx30G",
////        "-XX:+UseParallelGC",
//        "-XX:ConcGCThreads=15",
//        "-XX:ParallelGCThreads=5",
    "-XX:+UseZGC", "-XX:+ZGenerational",
//    "-XX:+UseShenandoahGC",
//        "-XX:ShenandoahGCMode=iu",
////        "-XX:+UnlockExperimentalVMOptions",
    "-XX:+UseNUMA",
//        "-XX:-UseBiasedLocking",
    "-XX:+UseLargePages", "-XX:+UseTransparentHugePages",
//
////        "-XX:+UsePerfData",
    "-XX:MaxMetaspaceSize=1G", "-XX:MetaspaceSize=256M",
    "-Xlog:gc*,gc+ref*,gc+ergo*,gc+heap*,gc+stats*,gc+compaction*,gc+age*:logs/gc.log:time,pid,tags:filecount=25,filesize=3000m"
}
)
public class MeasurementsBenchmarkTest {
    private final Measurements measurements = new Measurements(50);
    private final MeasurementsVolatile measurementsVolatile = new MeasurementsVolatile(50);
    private final long[] numbers = new long[30_000_000];
    private final AtomicInteger index = new AtomicInteger();

    @Setup
    public void loadData() throws Exception {
        index.set(0);
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = (int) ((Math.random() * (50 - 1)) + 1);
        }
    }

    @Benchmark
    public void measureUpdateAndGet(Blackhole blackhole) {
        blackhole.consume(measurements.measureUpdateAndGet(getRandom()));
    }

    @Benchmark
    public void measureGetSet(Blackhole blackhole) {
        blackhole.consume(measurements.measureGetSet(getRandom()));
    }

    @Benchmark
    public void measurementsVolatile(Blackhole blackhole) {
        blackhole.consume(measurementsVolatile.measure(getRandom()));
    }

    @Test
    public void test() throws Exception {
        main(null);
    }

    private long getRandom() {
        int i = index.getAndIncrement();

        if (i + 1 >= numbers.length) {
            index.set(0);
            i = 0;
        }

        return numbers[i];
    }

    @TearDown
    public void print() {
        System.err.println(measurements);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("drools.useEagerSegmentCreation", "true");
        if (args == null || args.length == 0)
            args = new String[]{"15"};

//        if (!file.exists() || !file.isDirectory())
//            file.mkdir();
        final Options options = new OptionsBuilder()
            .include(MeasurementsBenchmarkTest.class.getSimpleName())
            .forks(1)
//            .warmupIterations(2)
//            .measurementIterations(5)
            .threads(10)
//                .addProfiler(GCProfiler.class)
            .addProfiler(AsyncProfiler.class, "output=flamegraph;event=cpu;allkernel=true;direction=forward;interval=50000")
//                .addProfiler(LinuxPerfProfiler.class)
//                .addProfiler(LinuxPerfC2CProfiler.class)
            .build();

        new Runner(options).run();
//        new WorkerBenchmark().loadData();
    }
}
