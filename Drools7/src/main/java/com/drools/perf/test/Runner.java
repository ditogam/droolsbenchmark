package com.drools.perf.test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.drools.perf.test.helper.DroolsHelper;
import com.drools.perf.test.helper.Generator;
import com.drools.perf.test.model.Subscriber;

import static com.drools.perf.test.helper.DroolsHelper.executeSubscriber;

public class Runner {
    private AtomicInteger warmupIterations = new AtomicInteger();
    private AtomicInteger measurementIterations = new AtomicInteger();
    private int threadCount;
    private List<Subscriber> subscribers;
    private AtomicInteger index = new AtomicInteger();
    private Duration duration = Duration.ofSeconds(10);

    private void run() throws Exception {
        double full = 0;
        double min = Integer.MAX_VALUE;
        double max = Integer.MIN_VALUE;
        int measurementCount = 0;
        subscribers = Generator.getPreGeneratedSubscribers();
        while (true) {
            if (warmupIterations.get() <= 0 && measurementIterations.get() <= 0)
                break;
            boolean measurement = warmupIterations.get() <= 0;
            AtomicInteger count = new AtomicInteger();
            long time = System.currentTimeMillis();
            int index = 0;
            if (!measurement)
                index = warmupIterations.decrementAndGet();
            else
                index = measurementIterations.decrementAndGet();
//            System.out.println("Starting " + (!measurement ? "WARMUP :" : "measurement :") + "(" + (index) + ") ");
            Thread[] threads = runThreads(count);
//            System.out.println("Threads created " + (!measurement ? "WARMUP :" : "measurement :") + "(" + (index) + ") ");
            Thread.sleep(duration.toMillis());
//            System.out.println("Done sleeping/interrupting  " + (!measurement ? "WARMUP :" : "measurement :") + "(" + (index) + ") ");

            for (int i = 0; i < threads.length; i++) {
                threads[i].interrupt();
            }
//            System.out.println("Done interrupting/joining  " + (!measurement ? "WARMUP :" : "measurement :") + "(" + (index) + ") ");
            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
            }
            time = System.currentTimeMillis() - time;
            double executions = ((double) count.get() / (double) time) * 1000.0;
            System.out.println("Done " + (!measurement ? "WARMUP :" : "measurement :") + "(" + (index) + ") " + executions + " op/s" + " count  = " + count + " duration = " + time + " msc");
            if (measurement) {
                full += executions;
                measurementCount++;
                min = Math.min(min, executions);
                max = Math.max(max, executions);
            }

        }
        double avg = full / (double) measurementCount;
        double error = (Math.abs(min - avg) + Math.abs(max - avg)) / 2;
        System.err.println(String.format("Min = %s, Max = %s, avg = %s error = %s", min, max,avg, error));
        DroolsHelper.printResults();
    }

    private Thread[] runThreads(AtomicInteger count) throws Exception {

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        int i = index.incrementAndGet();
                        if (i + 1 >= subscribers.size()) {
                            index.set(0);
                            i = 0;
                        }
                        Subscriber subscriber = subscribers.get(i);
                        try {
                            executeSubscriber(subscriber);
                        } catch (Exception e) {

                        }
                        int val = count.incrementAndGet();
//                       if ( val%500==0)
//                           System.out.println("Done "+val);
                    }
                }
            });
            threads[i].start();
        }

        return threads;
    }

    public static void main(String[] args) throws Exception {
        Runner runner = new Runner();
        runner.warmupIterations.set(3);
        runner.measurementIterations.set(3);
        runner.threadCount = Runtime.getRuntime().availableProcessors() - 1;
        runner.run();
    }
}
