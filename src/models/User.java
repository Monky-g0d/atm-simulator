package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    private String cardNumber;
    private String pin;
    private boolean isBlocked;
    private int failedAttempts;
    private Account account;
    private final List<Transaction> transactions;

    public User(String cardNumber, String pin) {
        this(cardNumber, pin, new Account(0.0));
    }

    public User(String cardNumber, String pin, Account account) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.account = account;
        this.isBlocked = false;
        this.failedAttempts = 0;
        this.transactions = new ArrayList<>();
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPin() {
        return pin;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Account getAccount() {
        return account;
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            return;
        }

        transactions.add(transaction);

        while (transactions.size() > 10) {
            transactions.remove(0);
        }
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public List<Transaction> getRecentTransactions(int limit) {
        if (limit <= 0 || transactions.isEmpty()) {
            return Collections.emptyList();
        }

        int fromIndex = Math.max(0, transactions.size() - limit);
        return new ArrayList<>(transactions.subList(fromIndex, transactions.size()));
    }
}