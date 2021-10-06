package com.drools.perf.test.helper;

import com.drools.perf.test.model.Subscriber;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.StatelessSession;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderErrors;
import org.drools.rule.Package;
import org.drools.rule.Rule;
import org.drools.spi.Activation;
import org.drools.spi.AgendaFilter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DroolsHelper {
    public static final String DROOLS_FILE_NAME = "rools.data";

    private static final AtomicInteger EXECUTED_COUNT = new AtomicInteger();
    private static final AtomicInteger RULE_FOUND_COUNT = new AtomicInteger();
    private static Package PACKAGE = null;
    public static boolean threadLocal;
    public static boolean shadowProxy;


    public static void compile() throws Exception {
        PackageBuilder builder = new PackageBuilder();
        InputStreamReader roolIs = null;
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            roolIs = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("rools/main.drl"));
            fos = new FileOutputStream(DROOLS_FILE_NAME);
            builder.addPackageFromDrl(roolIs);
            oos = new ObjectOutputStream(fos);
            Package pkg = builder.getPackage();
            if (builder.hasErrors()) {
                PackageBuilderErrors errors = builder.getErrors();
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < errors.getErrors().length; i++)
                    stringBuilder.append(errors.getErrors()[i].getMessage()).append("\n");
                throw new IllegalArgumentException(stringBuilder.toString().trim());

            }
            oos.writeObject(pkg);
        } finally {
            if (roolIs != null)
                roolIs.close();
            if (oos != null)
                oos.close();
            if (fos != null)
                fos.close();

        }
    }

    public static void loadRules() throws Exception {
        if (PACKAGE == null) {
            synchronized (DroolsHelper.class) {
                if (PACKAGE == null) {
                    File file = new File(DROOLS_FILE_NAME);
                    if (!file.exists()) {
                        compile();
                    }
                    FileInputStream fis = null;
                    ObjectInputStream ois = null;
                    try {

                        fis = new FileInputStream(file);
                        ois = new ObjectInputStream(fis);
                        PACKAGE = (Package) ois.readObject();

                    } finally {
                        if (ois != null)
                            ois.close();
                        if (ois != null)
                            ois.close();

                    }
                }
            }
        }

    }

    private static final ThreadLocal<RuleBase> RULE_BASE_THREAD_LOCAL = new ThreadLocal<RuleBase>();
    private static RuleBase RULE_BASE = null;

    private static RuleBase ruleBase() throws Exception {
        loadRules();
        RuleBase ruleBase = null;
        if (threadLocal) {
            ruleBase = RULE_BASE_THREAD_LOCAL.get();
        } else {
            ruleBase = RULE_BASE;
        }
        if (ruleBase != null)
            return ruleBase;


        if (threadLocal) {
            ruleBase = createRuleBase();
            RULE_BASE_THREAD_LOCAL.set(ruleBase);
        } else {
            synchronized (DroolsHelper.class) {
                ruleBase = createRuleBase();
                RULE_BASE = ruleBase;
            }
        }

        return ruleBase;
    }

    private static RuleBase createRuleBase() throws Exception {
        RuleBaseConfiguration conf = new RuleBaseConfiguration();
        conf.setSequential(false);
        conf.setShadowProxy(shadowProxy);
        RuleBase ruleBase = RuleBaseFactory.newRuleBase(conf);
        ruleBase.addPackage(PACKAGE);
        return ruleBase;
    }

    public static void executeSubscriber(Subscriber subscriber) throws Exception {
        loadRules();
        StatelessSession ss = ruleBase().newStatelessSession();
        ArrayList<Object> objs = new ArrayList<Object>(subscriber.getAccounts().size() + 1);
        objs.add(subscriber);
        objs.addAll(subscriber.getAccounts());
        ss.setAgendaFilter(new AgendaFilter() {
            @Override
            public boolean accept(Activation activation) {
                Rule rule = activation.getRule();
                String activationGroup = rule.getActivationGroup();
                if (activationGroup != null && activationGroup.equals("main")) {
                    RULE_FOUND_COUNT.incrementAndGet();
                }
                return true;
            }
        });
        ss.execute(objs);
        EXECUTED_COUNT.incrementAndGet();
    }

    public static void printResults() {
//        List<String> list = RULE_RESULTS.stream().sorted().collect(Collectors.toList());
        System.err.printf("Executed = Found = %s Executed = %s. rule found = %s distinct rules %s%n",
                EXECUTED_COUNT.get() == RULE_FOUND_COUNT.get(), EXECUTED_COUNT, RULE_FOUND_COUNT, "");
    }

    public static void main(String[] args) throws Exception {
        List<Subscriber> subscribers = Generator.generateSubscribers(5);
        loadRules();
        for (Subscriber subscriber : subscribers) {
            executeSubscriber(subscriber);
        }
        printResults();

    }
}
