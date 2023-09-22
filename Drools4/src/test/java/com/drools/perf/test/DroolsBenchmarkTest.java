package com.drools.perf.test;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.drools.perf.test.helper.DroolsHelper;
import com.drools.perf.test.helper.Generator;
import com.drools.perf.test.model.Subscriber;
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
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.AsyncProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
//@BenchmarkMode(Mode.AverageTime)
//@OutputTimeUnit(TimeUnit.MICROSECONDS)

@State(Scope.Benchmark)
@Warmup(iterations = 150, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 500, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 2, jvmArgs = {"-Xms10G",
    "-XX:+UseZGC",
    "-XX:+ZGenerational",
//        "-XX:+UseParallelGC",
    "-Xlog:gc*,gc+ref*,gc+ergo*,gc+heap*,gc+stats*,gc+compaction*,gc+age*:logs/gc.log:time,pid,tags:filecount=25,filesize=3000m",
//    "-XX:+UseShenandoahGC",
//    "-javaagent:libs/jHiccup.jar",
//    "-XX:+UseZST",
    "-XX:MaxMetaspaceSize=1G", "-XX:MetaspaceSize=1G",
//        "-XX:StartFlightRecording=filename=myrecording.jfr,delay=60s",

        "-Xmx10G"/*,
        "-Xgc:deterministic", "-XpauseTarget=80ms", "-XXgcthreads:4"
        , "-Xverbose:compaction,gc,gcpause,gcreport,memory,memdbg",
        "-XverboseTimeStamp",
        "-Xverboselog:./logs/jvm.log"*/
}
)
public class DroolsBenchmarkTest {
    private List<Subscriber> subscribers;
    private AtomicInteger index = new AtomicInteger();

    //    @Param({"false", "true"})
    private boolean threadLocal = true;
//    @Param({"false", "true"})
    private boolean shadowProxy;

    @Setup
    public void loadData() throws Exception {
        DroolsHelper.threadLocal = threadLocal;
        DroolsHelper.shadowProxy = shadowProxy;
        subscribers = Generator.getPreGeneratedSubscribers();
    }

    @Benchmark
    public Subscriber send() throws Exception {
        int i = index.incrementAndGet();
        if (i + 1 >= subscribers.size()) {
            index.set(0);
            i = 0;
        }
        Subscriber subscriber = subscribers.get(i);
        DroolsHelper.executeSubscriber(subscriber);
        return subscriber;
    }

    @Test
    public void test() throws Exception {
        main(null);
    }

    @TearDown
    public void print() {
        DroolsHelper.printResults();
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0)
            args = new String[]{"10"};
        File file = new File("logs");
        if (!file.exists() || !file.isDirectory())
            file.mkdir();
        final Options options = new OptionsBuilder()
                .include(DroolsBenchmarkTest.class.getSimpleName())
                .forks(1)
            .threads(10)
            .addProfiler(AsyncProfiler.class, "output=flamegraph;event=cpu;allkernel=true;direction=forward;interval=50000")

//                .addProfiler(LinuxPerfProfiler.class)
                .build();

        new Runner(options).run();
//        new WorkerBenchmark().loadData();
    }
}
