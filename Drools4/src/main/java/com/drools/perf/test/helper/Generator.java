package com.drools.perf.test.helper;

import com.drools.perf.test.model.Account;
import com.drools.perf.test.model.Subscriber;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class Generator {
    public static final int MAX_CHARGING_PROFILE_ID = 50;
    public static final int MAX_ACCOUNT_KIND_ID = 80;
    public static final int MAX_ACC_TO_ADD = 5;
    private static final Random rd = new Random();
    private static final File SUBSCRIBERS_DATA = new File(new File(System.getProperty("user.dir")).getParent(), "subscribers.json");

    public static void generateRules() {
        int accountKindIds = MAX_ACCOUNT_KIND_ID;
        for (int chargingProfile = 0; chargingProfile < MAX_CHARGING_PROFILE_ID; chargingProfile++) {
            int salience = MAX_ACCOUNT_KIND_ID;
            for (int accountKindId = 0; accountKindId < MAX_ACCOUNT_KIND_ID; accountKindId++) {
                StringBuilder sb = new StringBuilder("rule \"general.test.rule.cp")
                        .append(chargingProfile).append(".").append("acc.").append(accountKindId).append("\"");
                sb.append("\n\tactivation-group \"main\"");
                sb.append("\n\tsalience ").append(salience);
                sb.append("\n\twhen ");
                sb.append("\n\t\tSubscriber(chargingProfileId == ").append(chargingProfile).append(")");
                sb.append("\n\t\t$acc:Account(accountKindId == ").append(accountKindId).append(", balance>0)");
                sb.append("\n\tthen");
                sb.append("\n\t\t$acc.decrementBalance();");
                sb.append("\nend\n\n");
                System.out.println(sb);
                salience--;
            }
        }
    }

    public static Subscriber generateSubscriber() {
        Subscriber subscriber = new Subscriber().setChargingProfileId(generateRandom(MAX_CHARGING_PROFILE_ID));
        int accCounts = generateRandom(1, MAX_ACC_TO_ADD);
        Set<Integer> accIds = new HashSet<Integer>();
        for (int i = 0; i < accCounts; i++) {
            while (!accIds.add(generateRandom(MAX_ACCOUNT_KIND_ID))) ;
        }
        List<Integer> list = new ArrayList<Integer>(accIds);
        Collections.shuffle(list);
        for (Integer accId : list) {
            subscriber.acc(accId, generateRandom(-1000, 1000));
        }
        boolean hasPositive = false;
        List<Account> accounts = subscriber.getAccounts();
        for (Account account : accounts) {
            if (account.getBalance() > 0) {
                hasPositive = true;
                break;
            }
        }
        if (!hasPositive) {
            accounts.get(0).setBalance(generateRandom(500, 10000));
        }
        return subscriber;
    }

    public static List<Subscriber> generateSubscribers(int count) {
        List<Subscriber> subscribers = new ArrayList<Subscriber>();
        for (int i = 0; i < count; i++) {
            subscribers.add(generateSubscriber());
        }
        return subscribers;
    }

    public static List<Subscriber> getPreGeneratedSubscribers() throws Exception {
        FileInputStream fos = null;
        GZIPInputStream gz = null;
        try {
            fos = new FileInputStream(SUBSCRIBERS_DATA);
            gz = new GZIPInputStream(fos);

            return new ObjectMapper().readValue(gz, new TypeReference<List<Subscriber>>() {
            });
        } finally {
            if (gz != null)
                gz.close();
            if (fos != null)
                fos.close();
        }
    }

    private static int generateRandom(int max) {
        return generateRandom(0, max);
    }

    private static int generateRandom(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getPreGeneratedSubscribers().size());
    }
}
