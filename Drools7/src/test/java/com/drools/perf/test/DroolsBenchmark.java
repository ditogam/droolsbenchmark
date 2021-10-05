package com.drools.perf.test;

import com.drools.perf.test.helper.DroolsHelper;
import com.drools.perf.test.helper.Generator;
import com.drools.perf.test.model.Subscriber;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//@BenchmarkMode(Mode.Throughput)
//@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)

@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms10G",
        "-Xmx10G"/*, "-Xgc:deterministic", "-XpauseTarget=80ms", "-XXgcthreads:4"
        , "-Xverbose:compaction,gc,gcpause,gcreport,memory,memdbg",
        "-XverboseTimeStamp",
        "-Xverboselog:./logs/jvm.log"*/
}
)
public class DroolsBenchmark {
    private List<Subscriber> subscribers;
    private AtomicInteger index = new AtomicInteger();

    //    @Param({"0", "3"})
    private int sessionPool;
//    @Param({"false", "true"})
//    private boolean threadLocal;

    @Setup
    public void loadData() throws Exception {
        DroolsHelper.sessionPool = sessionPool;
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
            args = new String[]{"10"};
        File file = new File("logs");
        if (!file.exists() || !file.isDirectory())
            file.mkdir();
        final Options options = new OptionsBuilder()
                .include(DroolsBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(5)
                .measurementIterations(30)
                .threads(Integer.parseInt(args[0]))
//                .addProfiler(GCProfiler.class)

                .build();

        new Runner(options).run();
//        new WorkerBenchmark().loadData();
    }
}
