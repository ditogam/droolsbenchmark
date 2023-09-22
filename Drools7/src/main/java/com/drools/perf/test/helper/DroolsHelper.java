package com.drools.perf.test.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.drools.perf.test.model.Account;
import com.drools.perf.test.model.Subscriber;
import org.drools.base.definitions.rule.impl.RuleImpl;
import org.drools.commands.runtime.rule.FireAllRulesCommand;
import org.drools.core.RuleBaseConfiguration;
import org.drools.io.ReaderResource;
import org.drools.kiesession.rulebase.InternalKnowledgeBase;
import org.drools.kiesession.rulebase.KnowledgeBaseFactory;
import org.drools.model.Model;
import org.drools.modelcompiler.KieBaseBuilder;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.definition.KiePackage;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;

public class DroolsHelper {
    public static final String DROOLS_FILE_NAME = "rools.data";
    private static final AtomicInteger EXECUTED_COUNT = new AtomicInteger();
    private static final AtomicInteger RULE_FOUND_COUNT = new AtomicInteger();
    private static final ReentrantLock lock = new ReentrantLock();
    public static final KieCommands KIE_COMMANDS = KieServices.Factory.get().getCommands();

    private static int sessionPool = 0;
    private static boolean threadLocal = true;
    private static boolean useCanonicalModel = true;
    private static Collection<KiePackage> kiePackages = null;

    private static Logger logger = Logger.getLogger(DroolsHelper.class.getName());

    private static void compile() throws Exception {
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("com/drools/perf/test/main.drl");
             Reader reader = new InputStreamReader(is);
             FileOutputStream fos = new FileOutputStream(DROOLS_FILE_NAME);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            builder.add(new ReaderResource(reader), ResourceType.DRL);
            Collection<KiePackage> kiePackages = builder.getKnowledgePackages();
            oos.writeObject(kiePackages);
        }
    }

    private static RuleBaseConfiguration getRuleBaseConfiguration() {
        RuleBaseConfiguration conf = KnowledgeBaseFactory.newKnowledgeBase().getRuleBaseConfiguration();
        conf.setSequential(false);
        if (sessionPool > 0)
            conf.setSessionPoolSize(sessionPool);
//        System.out.println("sessionPool============" + sessionPool);
        return conf;
    }

    private static void loadRules() throws Exception {
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
        }
        return null;


    }

    public static void createSingletonKieBase() throws Exception {
        loadRules();
        if (!threadLocal)
            RULE_BASE = createKieBase();
    }

    private static KieBase createKieBase() {

        if (useCanonicalModel) {
            try {
                Model model =
                    (Model) Class.forName("com.drools.perf.test.Rulese3c9e9a2f4a348d294459c573d758226").getConstructor().newInstance();
                KieBase kieBase = KieBaseBuilder.createKieBaseFromModel(model);

                return kieBase;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        InternalKnowledgeBase ruleBase = KnowledgeBaseFactory.newKnowledgeBase();

        ruleBase.addPackages(kiePackages);

        return ruleBase;
    }

    public static void init() throws Exception {
        init(0, false, false);
    }

    public static void init(int sessionPool, boolean threadLocal, boolean useCanonicalModel) throws Exception {
        DroolsHelper.sessionPool = sessionPool;
        DroolsHelper.threadLocal = threadLocal;
        DroolsHelper.useCanonicalModel = useCanonicalModel;
        createSingletonKieBase();
    }

    public static void executeSubscriber(Subscriber subscriber) throws Exception {

        ArrayList<Command<?>> objs = new ArrayList<>(3);
        objs.add(KIE_COMMANDS.newInsert(subscriber));
        objs.add(KIE_COMMANDS.newInsertElements(subscriber.getAccounts()));

        objs.add(new FireAllRulesCommand(match -> {
            RuleImpl rule = (RuleImpl) match.getRule();
            String activationGroup = rule.getActivationGroup();
            if (activationGroup != null && activationGroup.equals("main")) {
                RULE_FOUND_COUNT.incrementAndGet();
            }
            return true;
        }));
        BatchExecutionCommand exec = KIE_COMMANDS.newBatchExecution(objs);
        StatelessKieSession ss = Objects.requireNonNull(ruleBase()).newStatelessKieSession();
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
        init(2, true, true);
        List<Subscriber> subscribers = Generator.generateSubscribers(1);
        loadRules();
        for (Subscriber subscriber : subscribers) {
            executeSubscriber(subscriber);
        }
        logger.info("done");
        printResults();
    }
}
