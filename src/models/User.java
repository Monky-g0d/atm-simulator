package model;

public class User {
    private String cardNumber;
    private String pin;
    private boolean isBlocked;
    private int failedAttempts;
    private Account account;

    public User(String cardNumber, String pin, Account account) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.account = account;
        this.isBlocked = false;
        this.failedAttempts = 0;
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
}