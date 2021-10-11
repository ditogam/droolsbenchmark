package com.drools.perf.test.model;


import java.util.ArrayList;
import java.util.List;

public class Subscriber {
    private int chargingProfileId;
    private List<Account> accounts;

    public int getChargingProfileId() {
        return chargingProfileId;
    }

    public Subscriber setChargingProfileId(int chargingProfileId) {
        this.chargingProfileId = chargingProfileId;
        return this;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public Subscriber setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        return this;
    }

    public Subscriber acc(int accountKindId, long balance) {
        if (accounts == null)
            accounts = new ArrayList<Account>();
        accounts.add(new Account(accountKindId, balance));
        return this;
    }
}
