package benchmarks;

import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 30, time = 10, timeUnit = TimeUnit.SECONDS)

public class SynchronizationBenchmarkTest {
    private AtomicLong atomic = new AtomicLong();
    private volatile long volatileNumber = 0;
    private long nonVolatileNumber = 0;
    @Param("0")
    private int threadCount;
    private Object lock = new Object();

    @Setup
    public void init() {
        atomic.set(0);
        volatileNumber = 0;
        nonVolatileNumber = 0;
    }

    @Benchmark
    public void testAtomic(Blackhole bh) {
        bh.consume(atomic.incrementAndGet());
    }

    @Benchmark
    public void testVolatile(Blackhole bh) {
        bh.consume(incrementVolatile());
    }

    public synchronized long incrementVolatile() {
        return ++volatileNumber;
    }

    @Benchmark
    public void testNonVolatileNumber(Blackhole bh) {
        synchronized (lock) {
            bh.consume(++nonVolatileNumber);
        }
    }

    @Test
    public void test() throws Exception {
        main(null);
    }

    public static String replaceSpaces(String propertyName) {
        String value = System.getProperty(propertyName,"");
        value = value.replaceAll(" ", "__").replaceAll("\\+", "__");
        return value;
    }

    public static void main(String[] args) throws Exception {
        int[] threads = {1, 8};
        boolean useProfiler = false;

        useProfiler = Boolean.parseBoolean(System.getProperty("use.profiler", "false").trim());
        File file = new File("results");
        if (!file.isDirectory() || !file.exists())
            file.mkdirs();
        String filePrefixDefault = "results/" + "result_" + replaceSpaces("java.vm.name")
                + "_" + replaceSpaces("java.version")
                + "_" + replaceSpaces("java.vendor.version")
                + "_" + replaceSpaces("java.runtime.version");
        String filenamePrefix = System.getProperty("out.file.prefix", filePrefixDefault).trim();
        for (int threadCount : threads) {
            String fileName = filenamePrefix + "_tc" + threadCount + ".csv";
            List<String> jvmArgs = new ArrayList<String>(Arrays.asList("-Xms10G",
                    "-Xmx10G"));
            if (System.getProperty("java.vm.name").contains("JRockit")) {
                jvmArgs.add("-Xgc:parallel");
            } else
                jvmArgs.add("-XX:+UseParallelGC");
            ChainedOptionsBuilder builder = new OptionsBuilder()
                    .forks(1)
                    .include(SynchronizationBenchmarkTest.class.getSimpleName())
                    .threads(threadCount)
                    .param("threadCount", String.valueOf(threadCount))
                    .result(fileName)
                    .jvmArgs(jvmArgs.toArray(new String[0]))
                    .resultFormat(ResultFormatType.CSV);
            if (useProfiler)
                AddProfiler.addProfiler(builder);
            final Options options = builder
                    .build();
            new Runner(options).run();
        }
    }
}
