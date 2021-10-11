package benchmarks;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class DataToCSV extends JFrame implements WindowListener {

    public DataToCSV(String title, JTabbedPane tabbedPane) {
        super(title);
        this.addWindowListener(this);


        setContentPane(tabbedPane);

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (e.getWindow() == this) {
            this.dispose();
            System.exit(0);
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    private enum Threads {
        SINGLE("Single Thread"), MULTI_8("8 Threads");
        private String value;

        Threads(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private enum Machine {
        HOME, ORACLE_LINUX;

    }

    private enum SynchronizationType {
        ATOMIC("Atomic operations"), VOLATILE("Volatile operations"), SYNCHRONIZED("Synchronized operations");
        private String value;

        SynchronizationType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private enum JavaVersion {
        V_1_6("Java 6"), V_1_6JR("Jrockit realtime"), V_1_7("Java 7"), V_1_8("Java 8"), V_11("Java 11"), V_17("Java 17");
        private String value;

        JavaVersion(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static class BenchResult {
        private JavaVersion javaVersion;
        private Machine machine;
        private Threads threadCount;
        private SynchronizationType synchronizationType;
        private double throughput;
        private double errors;

        private boolean match(JavaVersion javaVersion, Machine machine, Threads threadCount, SynchronizationType synchronizationType) {
            return javaVersion.equals(this.javaVersion) && machine.equals(this.machine) && threadCount.equals(this.threadCount) && synchronizationType.equals(this.synchronizationType);
        }
    }

    private static List<BenchResult> readFile(File file, Machine machine, JavaVersion javaVersion) throws Exception {
        FileReader fileReader = new FileReader(file);
        CSVReader csvReader = new CSVReader(fileReader);
        List<BenchResult> result = new ArrayList<BenchResult>();
        try {
            // Session reader loop
            String[] tokens;


            while ((tokens = csvReader.readNext()) != null) {
                String type = tokens[0];
                if (type.startsWith("benchmarks.Synchronization") && !type.contains(":") && tokens[1].equals("thrpt")) {
                    BenchResult benchResult = new BenchResult();
                    if (type.endsWith(".testAtomic"))
                        benchResult.synchronizationType = SynchronizationType.ATOMIC;
                    if (type.endsWith(".testNonVolatileNumber"))
                        benchResult.synchronizationType = SynchronizationType.SYNCHRONIZED;
                    if (type.endsWith(".testVolatile"))
                        benchResult.synchronizationType = SynchronizationType.VOLATILE;
                    benchResult.threadCount = Integer.parseInt(tokens[2]) == 1 ? Threads.SINGLE : Threads.MULTI_8;
                    benchResult.throughput = Double.parseDouble(tokens[4]);
                    benchResult.errors = Double.parseDouble(tokens[5]);
                    benchResult.javaVersion = javaVersion;
                    benchResult.machine = machine;
                    result.add(benchResult);
                }


            }

        } finally {
            // Always close sessions CSV
            csvReader.close();
        }
        return result;
    }

    private static void listData(File dir, Machine machine, List<BenchResult> benchResults) throws Exception {
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getAbsolutePath().toLowerCase().endsWith(".csv");
            }
        });
        for (File file : files) {
            JavaVersion javaVersion = null;
            String fileName = file.getName().toLowerCase();
            if (fileName.contains("1.6")) {
                javaVersion = fileName.contains("jrockit") ? JavaVersion.V_1_6JR : JavaVersion.V_1_6;
            }
            if (fileName.contains("1.7")) {
                javaVersion = JavaVersion.V_1_7;
            }
            if (fileName.contains("1.8")) {
                javaVersion = JavaVersion.V_1_8;
            }
            if (fileName.contains("_11_")) {
                javaVersion = JavaVersion.V_11;
            }
            if (fileName.contains("_17_")) {
                javaVersion = JavaVersion.V_17;
            }
            benchResults.addAll(readFile(file, machine, javaVersion));
        }
    }

    public static void main(String[] args) throws Exception {
        List<BenchResult> benchResults = new ArrayList<BenchResult>();
        listData(new File("resultsHome"), Machine.HOME, benchResults);
        listData(new File("resultsOracle"), Machine.ORACLE_LINUX, benchResults);
        JTabbedPane tabbedPane = new JTabbedPane();

        for (Threads threads : Threads.values()) {
            JTabbedPane tabbedPaneJava = new JTabbedPane();
            for (SynchronizationType synchronizationType : SynchronizationType.values()) {

                final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                // create the chart...
                final JFreeChart chart = ChartFactory.createBarChart(
                        threads.toString() + "/" + synchronizationType.toString(),        // chart title
                        "Java version",               // domain axis label
                        "Operation per second",                  // range axis label
                        dataset,                 // data
                        PlotOrientation.VERTICAL,
                        true,                     // include legend
                        true,                     // tooltips?
                        false                     // URL generator?  Not required...
                );

                // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
                chart.setBackgroundPaint(Color.white);

                CategoryPlot plot = chart.getCategoryPlot();
                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                NumberFormat formatter = NumberFormat.getIntegerInstance();

                rangeAxis.setNumberFormatOverride(new DecimalFormat("#0.##"));

                rangeAxis.setNumberFormatOverride(formatter);
                final ChartPanel chartPanel = new ChartPanel(chart);
                chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
                for (JavaVersion javaVersion : JavaVersion.values()) {
                    for (Machine machine : Machine.values()) {
                        String title = javaVersion + "/" + machine + "/" + threads + "/" + synchronizationType;
                        BenchResult benchResult = null;
                        for (BenchResult result : benchResults) {
                            if (result.match(javaVersion, machine, threads, synchronizationType)) {
                                benchResult = result;
                                break;
                            }
                        }
                        if (benchResult == null)
                            throw new IllegalArgumentException("Result not found for " + title);
                        dataset.addValue(benchResult.throughput, machine, javaVersion);
                    }

                }

                FileOutputStream fos = null;
                try {
                    BufferedImage bufferedImage = chart.createBufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB, null);
                    fos = new FileOutputStream(new File("fulloutput/imgs/" + threads.name() + "_" + synchronizationType.name() + ".png"));
                    ImageIO.write(bufferedImage, "png", fos);
                } finally {
                    if (fos != null)
                        fos.close();
                }


                tabbedPaneJava.add(synchronizationType.toString(), chartPanel);
            }
            tabbedPane.add(threads.toString(), tabbedPaneJava);
        }
        final DataToCSV demo = new DataToCSV("Dual Axis Demo", tabbedPane);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }

    private static void saveToCsv(List<BenchResult> benchResults) throws IOException {
        List<String> titles = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        int index = 0;
        DecimalFormat df = new DecimalFormat("#0.##");

        for (JavaVersion value : JavaVersion.values()) {

            for (Threads threads : Threads.values()) {
                for (SynchronizationType synchronizationType : SynchronizationType.values()) {
                    for (Machine machine : Machine.values()) {
                        String title = value + "/" + machine + "/" + threads + "/" + synchronizationType;
                        BenchResult benchResult = null;
                        for (BenchResult result : benchResults) {
                            if (result.match(value, machine, threads, synchronizationType)) {
                                benchResult = result;
                                break;
                            }
                        }
                        if (benchResult == null)
                            throw new IllegalArgumentException("Result not found for " + title);
                        titles.add(title);
                        values.add(df.format(benchResult.throughput));
                        index++;
                    }
                }
            }
        }
        List<String[]> data = new ArrayList<String[]>();
        data.add(titles.toArray(new String[0]));
        data.add(values.toArray(new String[0]));
        FileWriter fws = new FileWriter("finalResults.csv");
        CSVWriter cwr = new CSVWriter(fws);
        try {
            cwr.writeAll(data);

        } finally {
            // Always close bad rule csv writer
            cwr.close();
        }
    }


}
