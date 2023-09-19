package com.drools.perf.test.model;


import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class Subscriber implements java.io.Serializable {
    static final long serialVersionUID = 1L;

    private int chargingProfileId;
    private List<Account> accounts;

    public Subscriber setChargingProfileId(int chargingProfileId) {
        this.chargingProfileId = chargingProfileId;
        return this;
    }


    public Subscriber setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        return this;
    }

    public Subscriber acc(int accountKindId, long balance) {
        if (accounts == null)
            accounts = new ArrayList<>();
        accounts.add(new Account(accountKindId, balance));
        return this;
    }
}
