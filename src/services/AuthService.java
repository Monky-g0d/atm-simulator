package services;

import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.List;
import models.User;
import models.Transaction;

public class AuthService {

    // 1. ЛОГИКА ВХОДА
    public User login(HashMap<String, User> users, String cardNumber, String pin) {
        User user = users.get(cardNumber);

        if (user == null) {
            System.out.println("Пользователь не найден");
            return null;
        }

        if (user.isBlocked()) {
            System.out.println("Карта заблокирована");
            return null;
        }

        if (user.getPin().equals(pin)) {
            user.setFailedAttempts(0); 
            return user;
        } else {
            int currentAttempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(currentAttempts);

            if (currentAttempts >= 3) {
                user.setBlocked(true);
                System.out.println("Карта заблокирована!");
            } else {
                System.out.println("Неверный PIN. Осталось попыток: " + (3 - currentAttempts));
            }
            return null;
        }
    }

    // 2. ФИНАНСОВЫЕ ОПЕРАЦИИ
    public void deposit(User user, double amount) {
        if (amount > 0) {
            double newBalance = user.getAccount().getBalance() + amount;
            user.getAccount().setBalance(newBalance);
            addTransaction(user, "Пополнение", amount);
            System.out.println("Баланс пополнен.");
        }
    }

    public void withdraw(User user, double amount) {
        if (amount > 0 && user.getAccount().getBalance() >= amount) {
            double newBalance = user.getAccount().getBalance() - amount;
            user.getAccount().setBalance(newBalance);
            addTransaction(user, "Снятие наличных", amount);
            System.out.println("Снятие выполнено.");
        } else {
            System.out.println("Ошибка: недостаточно средств.");
        }
    }

    public void transfer(User sender, HashMap<String, User> allUsers, String receiverCard, double amount) {
        User receiver = allUsers.get(receiverCard);
        
        if (receiver != null && !sender.getCardNumber().equals(receiverCard) && amount > 0) {
            if (sender.getAccount().getBalance() >= amount) {
                sender.getAccount().setBalance(sender.getAccount().getBalance() - amount);
                receiver.getAccount().setBalance(receiver.getAccount().getBalance() + amount);
                
                addTransaction(sender, "Перевод на " + receiverCard, amount);
                addTransaction(receiver, "Приход от " + sender.getCardNumber(), amount);
                System.out.println("Перевод выполнен.");
            } else {
                System.out.println("Недостаточно средств.");
            }
        }
    }

    // 3. ИСТОРИЯ
    private void addTransaction(User user, String type, double amount) {
        List<Transaction> history = user.getAccount().getHistory();
        
        history.add(new Transaction(type, amount, LocalDateTime.now()));

        if (history.size() > 10) {
            history.remove(0);
        }
    }
}