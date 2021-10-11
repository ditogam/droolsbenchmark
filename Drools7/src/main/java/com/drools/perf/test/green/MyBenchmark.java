package com.drools.perf.test.green;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.annotations.Threads;

@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(value = 1, warmups = 0)
@Threads(10)
@BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
public class MyBenchmark {

    @Benchmark
    public void testParallelArraySort_10(ParallelState state) {
        state.setSize(10);
    }

    @Benchmark
    public void testParallelArraySort_20(ParallelState state) {
        state.setSize(20);
    }

    @Benchmark
    public void testSequentialArraySort_10(ParallelState state) {
        state.setSize(10);
    }

    @Benchmark
    public void testSequentialArraySort_20(ParallelState state) {
        state.setSize(20);
    }

    @State(Scope.Thread)
    public static class SequentialState extends BaseState {

        @Setup(Level.Invocation)
        public void init() {
            array = generateArray();
        }

        @TearDown(Level.Invocation)
        public void destroy() {
            array = null;
        }

        public void run() {
            MathHelper.sequenatialSort(array);
        }
    }

    @State(Scope.Thread)
    public static class ParallelState extends BaseState {

        @Setup(Level.Invocation)
        public void init() {
            array = generateArray();
        }

        @TearDown(Level.Invocation)
        public void destroy() {
            array = null;
        }

        public void run() {
            MathHelper.parallelSort(array);
        }
    }
    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
