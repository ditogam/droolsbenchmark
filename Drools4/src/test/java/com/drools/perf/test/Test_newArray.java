package com.drools.perf.test;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import static org.openjdk.jmh.annotations.Threads.MAX;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Threads(MAX)
public class Test_newArray {
    private static int num = 10000;
    private static int length = 10;

    @Benchmark
    public static int[][] newArray() {
        return new int[num][length];
    }

    @Benchmark
    public static int[][] newArray2() {
        int[][] temps = new int[num][];
        for (int i = 0; i < temps.length; i++) {
            temps[i] = new int[length];
        }
        return temps;
    }
}