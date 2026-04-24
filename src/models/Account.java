package models;

import exceptions.ATMException;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private double balance;
    private List<Transaction> history;

    public Account(double balance) {
        this.balance = balance;
        this.history = new ArrayList<>();
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<Transaction> getHistory() {
        return history;
    }

    public void deposit(double amount) throws ATMException {
        if (amount <= 0) {
            throw new ATMException("Сумма пополнения должна быть больше нуля");
        }
        balance += amount;
    }

    public void withdraw(double amount) throws ATMException {
        if (amount <= 0) {
            throw new ATMException("Сумма снятия должна быть больше нуля");
        }
        if (amount > balance) {
            throw new ATMException("Недостаточно средств на счете");
        }
        balance -= amount;
    }
}