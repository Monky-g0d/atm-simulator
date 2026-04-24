package service;

import model.User;
import java.util.HashMap;

public class AuthService {

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
            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() >= 3) {
                user.setBlocked(true);
                System.out.println("Карта заблокирована!");
            } else {
                System.out.println("Неверный PIN");
            }

            return null;
        }
    }
}