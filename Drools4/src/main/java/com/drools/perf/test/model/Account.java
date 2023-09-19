package com.drools.perf.test.model;

import lombok.Getter;

@Getter
public class Account {
    private int accountKindId;
    private long balance;

    public Account() {
    }

    public Account(int accountKindId, long balance) {
        this.accountKindId = accountKindId;
        this.balance = balance;
    }

    public Account setAccountKindId(int accountKindId) {
        this.accountKindId = accountKindId;
        return this;
    }

    public Account setBalance(long balance) {
        this.balance = balance;
        return this;
    }

    public void decrementBalance(){
        balance--;
    }
}
