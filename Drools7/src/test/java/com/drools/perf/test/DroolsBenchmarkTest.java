package com.drools.perf.test;

import com.drools.perf.test.helper.DroolsHelper;
import com.drools.perf.test.helper.Generator;
import com.drools.perf.test.model.Subscriber;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.AsyncProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Threads(10)
//@BenchmarkMode(Mode.Throughput)
//@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)

@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms10G",
        "-Xmx10G",
////        "-XX:+UseParallelGC",
//        "-XX:ConcGCThreads=5",
//        "-XX:ParallelGCThreads=5",
//        "-XX:+UseZGC",
//        "-XX:+UseShenandoahGC",
////        "-XX:ShenandoahGCMode=iu",
////        "-XX:+UnlockExperimentalVMOptions",
//        "-XX:+UseNUMA",
//        "-XX:-UseBiasedLocking",
//        "-XX:+UseLargePages", "-XX:+UseTransparentHugePages",
//
////        "-XX:+UsePerfData",
////        "-XX:MaxMetaspaceSize=1G", "-XX:MetaspaceSize=256M",
//        "-Xlog:gc*,gc+ref*,gc+ergo*,gc+heap*,gc+stats*,gc+compaction*,gc+age*:logs/gc.log:time,pid,tags:filecount=25,filesize=30m"
}
)
public class DroolsBenchmarkTest {
    private List<Subscriber> subscribers;
    private AtomicInteger index = new AtomicInteger();

    //    @Param({"0", "3"})
    private int sessionPool;
    //    @Param({"false", "true"})
    private boolean threadLocal;

    @Setup
    public void loadData() throws Exception {
        DroolsHelper.init(sessionPool, threadLocal);
//        DroolsHelper.threadLocal = threadLocal;
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
            args = new String[]{"15"};

//        if (!file.exists() || !file.isDirectory())
//            file.mkdir();
        final Options options = new OptionsBuilder()
                .include(DroolsBenchmarkTest.class.getSimpleName())
                .forks(1)
                .warmupIterations(5)
                .measurementIterations(10)
                .threads(Integer.parseInt(args[0]))
//                .addProfiler(GCProfiler.class)
//                .addProfiler(AsyncProfiler.class, "output=flamegraph;event=cpu;allkernel=true;direction=forward;interval=50000")
//                .addProfiler(LinuxPerfProfiler.class)
//                .addProfiler(LinuxPerfC2CProfiler.class)
                .build();

        new Runner(options).run();
//        new WorkerBenchmark().loadData();
    }
}
