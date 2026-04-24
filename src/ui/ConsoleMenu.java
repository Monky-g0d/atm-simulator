package ui;

import exceptions.ATMException;
import models.User;
import services.AuthService;
import services.ValidationService;

import java.util.HashMap;
import java.util.Scanner;

public class ConsoleMenu {
    private final HashMap<String, User> users;
    private final AuthService authService;
    private final ValidationService validationService;
    private final Scanner scanner;
    private boolean running = true;

    public ConsoleMenu(HashMap<String, User> users, AuthService authService, ValidationService validationService, Scanner scanner) {
        this.users = users;
        this.authService = authService;
        this.validationService = validationService;
        this.scanner = scanner;
    }

    public void start() {
        System.out.println("Добро пожаловать в ATM Simulator");
        while (running) {
            System.out.println("\n--- Главное окно ---");
            System.out.println("1. Вход в систему");
            System.out.println("2. Регистрация");
            System.out.println("3. Выход");
            System.out.print("Выберите пункт: ");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> handleLogin();
                    case "2" -> handleRegistration();
                    case "3" -> running = false;
                    default -> System.out.println("Неверный пункт меню.");
                }
            } catch (ATMException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void handleLogin() throws ATMException {
        System.out.print("Введите номер карты: ");
        String card = validationService.requireCardNumber(scanner.nextLine());
        System.out.print("Введите PIN: ");
        String pin = validationService.requirePin(scanner.nextLine());

        User user = authService.login(users, card, pin);
        if (user != null) {
            System.out.println("Вход выполнен успешно.");
            userMenu(user);
        }
    }

    private void handleRegistration() throws ATMException {
        System.out.println("\n--- Регистрация ---");
        System.out.print("Введите номер карты (4-19 цифр): ");
        String card = validationService.requireCardNumber(scanner.nextLine());

        if (users.containsKey(card)) {
            throw new ATMException("Пользователь с таким номером карты уже существует");
        }

        System.out.print("Введите PIN (4 цифры): ");
        String pin = validationService.requirePin(scanner.nextLine());

        users.put(card, new User(card, pin));
        System.out.println("Регистрация успешно завершена.");
    }

    private void userMenu(User user) {
        boolean inAccount = true;
        while (inAccount) {
            System.out.println("\n--- Меню пользователя ---");
            System.out.println("1. Просмотр баланса");
            System.out.println("2. Пополнение");
            System.out.println("3. Снятие");
            System.out.println("4. Перевод");
            System.out.println("5. История транзакций");
            System.out.println("6. Выйти из аккаунта");
            System.out.print("Выберите пункт: ");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> System.out.printf("Текущий баланс: %.2f\n", user.getAccount().getBalance());
                    case "2" -> {
                        System.out.print("Введите сумму пополнения: ");
                        double amount = validationService.parsePositiveAmount(scanner.nextLine());
                        authService.deposit(user, amount);
                    }
                    case "3" -> {
                        System.out.print("Введите сумму снятия: ");
                        double amount = validationService.parsePositiveAmount(scanner.nextLine());
                        authService.withdraw(user, amount);
                    }
                    case "4" -> {
                        System.out.print("Введите карту получателя: ");
                        String targetCard = scanner.nextLine();
                        System.out.print("Введите сумму перевода: ");
                        double amount = validationService.parsePositiveAmount(scanner.nextLine());
                        authService.transfer(user, users, targetCard, amount);
                    }
                    case "5" -> {
                        var history = user.getRecentTransactions(10);
                        if (history.isEmpty()) {
                            System.out.println("История транзакций пуста.");
                        } else {
                            history.forEach(t -> System.out.printf("%s | %s | %.2f\n",
                                    t.getDate(), t.getType(), t.getAmount()));
                        }
                    }
                    case "6" -> inAccount = false;
                    default -> System.out.println("Неверный пункт.");
                }
            } catch (ATMException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }
}