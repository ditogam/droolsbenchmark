package com.drools.perf.test.model;

public class Account {
    private int accountKindId;
    private long balance;

    public Account() {
    }

    public Account(int accountKindId, long balance) {
        this.accountKindId = accountKindId;
        this.balance = balance;
    }

    public int getAccountKindId() {
        return accountKindId;
    }

    public Account setAccountKindId(int accountKindId) {
        this.accountKindId = accountKindId;
        return this;
    }

    public long getBalance() {
        return balance;
    }

    public Account setBalance(long balance) {
        this.balance = balance;
        return this;
    }

    public void decrementBalance(){
        balance--;
    }
}
