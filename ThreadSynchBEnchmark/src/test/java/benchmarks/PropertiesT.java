package benchmarks;

import org.junit.Test;

import java.util.Enumeration;

public class PropertiesT {
    @Test
    public void printProperties() {
        Enumeration<?> names = System.getProperties().propertyNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement().toString();
            if (name.startsWith("java.") || name.startsWith("sun.")) {
                System.out.println("Name=" + name + " value=" + SynchronizationBenchmarkTest.replaceSpaces(name));
            }
        }
    }
}
