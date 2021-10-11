package benchmarks;

import org.openjdk.jmh.profile.Profiler;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

import java.lang.reflect.Method;

public class AddProfiler {
    public static void addProfiler(ChainedOptionsBuilder builder) throws Exception {

        builder.addProfiler((Class<? extends Profiler>) Class.forName("org.openjdk.jmh.profile.LinuxPerfProfiler"));
        Method method = ChainedOptionsBuilder.class.getMethod("addProfiler", Class.class, String.class);
        method.invoke(builder, (Class<? extends Profiler>) Class.forName("org.openjdk.jmh.profile.AsyncProfiler"), "dir=flamegraphs;output=flamegraph;event=cpu;allkernel=true;direction=forward;interval=50000");

    }
}
