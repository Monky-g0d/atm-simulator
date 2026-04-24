package model;

import exceptions.ATMException;

public class Account {
    private double balance;

    public Account(double balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
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