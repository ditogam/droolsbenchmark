package com.drools.perf.test;

import com.drools.perf.test.model.Subscriber;
import org.drools.model.Model;
import org.drools.modelcompiler.KieBaseBuilder;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleTest {
    static final Logger LOG = LoggerFactory.getLogger(RuleTest.class);

//    @Test
//
//    public void test() throws Exception {
//        Model model = (Model) Class.forName("com.drools.perf.test.Rules5e1ad983e4194696aff58025268aaf38").getConstructor().newInstance();
//        KieBase kieBase = KieBaseBuilder.createKieBaseFromModel(model);
//
//        StatelessKieSession ss = kieBase.newStatelessKieSession();
//        ss.setGlobal("result", "Hello");
//
//        ss.execute(new Subscriber().setChargingProfileId(15).acc(1, 1564));
//    }
}