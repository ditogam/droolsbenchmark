package com.drools.perf.test.helper;

import com.drools.perf.test.model.Subscriber;
import org.drools.core.RuleBaseConfiguration;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.io.impl.ReaderResource;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.definition.KiePackage;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DroolsHelper {
    public static final String DROOLS_FILE_NAME = "rools.data";
    private static final AtomicInteger EXECUTED_COUNT = new AtomicInteger();
    private static final AtomicInteger RULE_FOUND_COUNT = new AtomicInteger();

    public static int sessionPool = 0;
    private static Collection<KiePackage> kiePackages = null;
    public static boolean threadLocal;


    public static void compile() throws Exception {
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("rools/main.drl");
             Reader reader = new InputStreamReader(is);
             FileOutputStream fos = new FileOutputStream(DROOLS_FILE_NAME);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            builder.add(new ReaderResource(reader), ResourceType.DRL);
            Collection<KiePackage> kiePackages = builder.getKnowledgePackages();
            oos.writeObject(kiePackages);
        }
    }

    private static RuleBaseConfiguration getRuleBaseConfiguration() {
        RuleBaseConfiguration conf = (RuleBaseConfiguration) KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        conf.setSequential(false);
        if (sessionPool > 0)
            conf.setSessionPoolSize(sessionPool);
//        System.out.println("sessionPool============" + sessionPool);
        return conf;
    }

    public static void loadRules() throws Exception {
        if (kiePackages == null) {
            synchronized (DroolsHelper.class) {
                if (kiePackages == null) {
                    File file = new File(DROOLS_FILE_NAME);
                    if (!file.exists()) {
                        compile();
                    }
                    try (FileInputStream fis = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(fis)) {
                        kiePackages = (Collection<KiePackage>) ois.readObject();
                    }
                }
            }
        }

    }

    private static final ThreadLocal<KieBase> RULE_BASE_THREAD_LOCAL = new ThreadLocal<>();
    private static KieBase RULE_BASE = null;

    private static KieBase ruleBase() throws Exception {
        loadRules();
        KieBase ruleBase = null;
        if (threadLocal) {
            ruleBase = RULE_BASE_THREAD_LOCAL.get();
        } else {
            ruleBase = RULE_BASE;
        }
        if (ruleBase != null)
            return ruleBase;

        if (threadLocal) {
            ruleBase = createKieBase();
            RULE_BASE_THREAD_LOCAL.set(ruleBase);
            return ruleBase;
        } else {
            synchronized (DroolsHelper.class) {
                RULE_BASE = createKieBase();
                return RULE_BASE;
            }
        }


    }

    private static KieBase createKieBase() {
        KieBase ruleBase;
        ruleBase = KnowledgeBaseFactory.newKnowledgeBase(getRuleBaseConfiguration());
        ((KnowledgeBaseImpl) ruleBase).addPackages(kiePackages);
        return ruleBase;
    }

    public static void executeSubscriber(Subscriber subscriber) throws Exception {
        loadRules();
        KieCommands cmds = KieServices.Factory.get().getCommands();
        ArrayList<Object> objs = new ArrayList<Object>(subscriber.getAccounts().size() + 1);
        objs.add(subscriber);
        objs.addAll(subscriber.getAccounts());
        List<Command<?>> batch = new ArrayList<>();
        for (Object obj : objs) {
            batch.add(cmds.newInsert(obj));
        }
        batch.add(new FireAllRulesCommand(match -> {
            RuleImpl rule = (RuleImpl) match.getRule();
            String activationGroup = rule.getActivationGroup();
            if (activationGroup != null && activationGroup.equals("main")) {
                RULE_FOUND_COUNT.incrementAndGet();
            }
            return true;
        }));
        BatchExecutionCommand exec = cmds.newBatchExecution(batch);
        StatelessKieSession ss = ruleBase().newStatelessKieSession();
        ss.setGlobal("result", "Hello");

        ss.execute(exec);
        EXECUTED_COUNT.incrementAndGet();
    }

    public static void printResults() {
//        List<String> list = RULE_RESULTS.stream().sorted().collect(Collectors.toList());
        System.err.printf("Executed = Found = %s Executed = %s. rule found = %s distinct rules %s%n",
                EXECUTED_COUNT.get() == RULE_FOUND_COUNT.get(), EXECUTED_COUNT, RULE_FOUND_COUNT, "");
    }

    public static void main(String[] args) throws Exception {
        List<Subscriber> subscribers = Generator.generateSubscribers(1);
        loadRules();
        for (Subscriber subscriber : subscribers) {
            executeSubscriber(subscriber);
        }

    }
}
