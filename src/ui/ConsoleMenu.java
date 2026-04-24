package ui;

import exceptions.ATMException;
import model.Transaction;
import model.User;
import service.AuthService;
import service.ValidationService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final HashMap<String, User> users;
    private final AuthService authService;
    private final ValidationService validationService;
    private final Scanner scanner;
    private boolean running = true;

    public ConsoleMenu(HashMap<String, User> users,
                       AuthService authService,
                       ValidationService validationService,
                       Scanner scanner) {
        this.users = users;
        this.authService = authService;
        this.validationService = validationService;
        this.scanner = scanner;
    }

    public void start() {
        System.out.println("========================================");
        System.out.println("   Добро пожаловать в ATM Simulator");
        System.out.println("========================================");

        while (running) {
            showWelcomeMenu();
        }
    }

    private void showWelcomeMenu() {
        try {
            System.out.println();
            System.out.println("=========== Главное окно ===========");
            System.out.println("1. Вход в систему");
            System.out.println("2. Регистрация");
            System.out.println("3. Выход");
            System.out.print("Выберите пункт: ");

            String choiceInput = readLine();
            if (choiceInput == null) {
                return;
            }

            int choice = parseMenuChoice(choiceInput);

            switch (choice) {
                case 1:
                    User currentUser = authorizeUser();
                    if (currentUser != null) {
                        mainMenu(currentUser);
                    }
                    break;
                case 2:
                    registerUser();
                    break;
                case 3:
                    running = false;
                    break;
                default:
                    System.out.println("Неверный пункт меню.");
                    break;
            }
        } catch (ATMException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private User authorizeUser() {
        while (running) {
            try {
                System.out.println();
                System.out.println("Введите номер карты или 'exit' для выхода:");
                String cardInput = readLine();
                if (cardInput == null) {
                    return null;
                }

                String cardNumber = validationService.requireCardNumber(cardInput);

                if (isExitCommand(cardNumber)) {
                    return null;
                }

                System.out.println("Введите PIN:");
                String pinInput = readLine();
                if (pinInput == null) {
                    return null;
                }

                String pin = validationService.requirePin(pinInput);

                if (isExitCommand(pin)) {
                    return null;
                }

                User user = authService.login(users, cardNumber, pin);
                if (user != null) {
                    System.out.println("Вход выполнен успешно.");
                    return user;
                }
            } catch (ATMException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }

        return null;
    }

    private void registerUser() {
        try {
            System.out.println();
            System.out.println("=========== Регистрация ===========");

            System.out.print("Введите номер карты (4-19 цифр): ");
            String cardNumber = validationService.requireCardNumber(readLine());

            if (users.containsKey(cardNumber)) {
                throw new ATMException("Пользователь с таким номером карты уже существует");
            }

            System.out.print("Введите PIN (4 цифры): ");
            String pin = validationService.requirePin(readLine());

            System.out.print("Повторите PIN: ");
            String pinConfirm = validationService.requirePin(readLine());

            if (!pin.equals(pinConfirm)) {
                throw new ATMException("PIN и подтверждение PIN не совпадают");
            }

            User user = authService.register(users, cardNumber, pin);
            System.out.println("Регистрация успешно завершена. Можно войти в систему.");
            System.out.println("Номер карты: " + user.getCardNumber());
        } catch (ATMException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void mainMenu(User currentUser) {
        boolean sessionActive = true;

        while (running && sessionActive) {
            try {
                System.out.println();
                System.out.println("=========== Главное меню ===========");
                System.out.println("1. Просмотр баланса");
                System.out.println("2. Пополнение");
                System.out.println("3. Снятие");
                System.out.println("4. Перевод");
                System.out.println("5. История транзакций (последние 10 операций)");
                System.out.println("6. Выйти из аккаунта");
                System.out.print("Выберите пункт: ");

                String choiceInput = readLine();
                if (choiceInput == null) {
                    running = false;
                    return;
                }

                int choice = parseMenuChoice(choiceInput);

                switch (choice) {
                    case 1:
                        showBalance(currentUser);
                        break;
                    case 2:
                        deposit(currentUser);
                        break;
                    case 3:
                        withdraw(currentUser);
                        break;
                    case 4:
                        transfer(currentUser);
                        break;
                    case 5:
                        showTransactions(currentUser);
                        break;
                    case 6:
                        sessionActive = false;
                        System.out.println("Выход из аккаунта выполнен.");
                        break;
                    default:
                        System.out.println("Неверный пункт меню.");
                        break;
                }
            } catch (ATMException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void showBalance(User user) {
        System.out.printf("Текущий баланс: %.2f%n", user.getAccount().getBalance());
    }

    private void deposit(User user) throws ATMException {
        System.out.print("Введите сумму пополнения: ");
        double amount = validationService.parsePositiveAmount(readLine());

        user.getAccount().deposit(amount);
        user.addTransaction(new Transaction("DEPOSIT", amount));

        System.out.printf("Счет пополнен на %.2f. Новый баланс: %.2f%n", amount, user.getAccount().getBalance());
    }

    private void withdraw(User user) throws ATMException {
        System.out.print("Введите сумму снятия: ");
        double amount = validationService.parsePositiveAmount(readLine());

        user.getAccount().withdraw(amount);
        user.addTransaction(new Transaction("WITHDRAW", amount));

        System.out.printf("Снято %.2f. Новый баланс: %.2f%n", amount, user.getAccount().getBalance());
    }

    private void transfer(User user) throws ATMException {
        System.out.print("Введите номер карты получателя: ");
        String targetCardNumber = validationService.requireText(readLine(), "Введите номер карты получателя");

        if (user.getCardNumber().equals(targetCardNumber)) {
            throw new ATMException("Нельзя перевести деньги на собственную карту");
        }

        User targetUser = users.get(targetCardNumber);
        if (targetUser == null) {
            throw new ATMException("Карта получателя не найдена");
        }

        System.out.print("Введите сумму перевода: ");
        double amount = validationService.parsePositiveAmount(readLine());

        user.getAccount().withdraw(amount);
        targetUser.getAccount().deposit(amount);

        user.addTransaction(new Transaction("TRANSFER_OUT", amount));
        targetUser.addTransaction(new Transaction("TRANSFER_IN", amount));

        System.out.printf("Перевод %.2f выполнен успешно.%n", amount);
    }

    private void showTransactions(User user) {
        List<Transaction> transactions = user.getRecentTransactions(10);

        if (transactions.isEmpty()) {
            System.out.println("История транзакций пуста.");
            return;
        }

        System.out.println("Последние операции:");
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction transaction = transactions.get(i);
            System.out.printf("%s | %s | %.2f%n",
                    transaction.getDate().format(DATE_FORMATTER),
                    transaction.getType(),
                    transaction.getAmount());
        }
    }

    private int parseMenuChoice(String value) throws ATMException {
        try {
            return Integer.parseInt(validationService.requireText(value, "Выберите пункт меню"));
        } catch (NumberFormatException e) {
            throw new ATMException("Введите номер пункта меню");
        }
    }

    private boolean isExitCommand(String value) {
        return "exit".equalsIgnoreCase(value) || "q".equalsIgnoreCase(value);
    }

    private String readLine() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }

        running = false;
        return null;
    }
}


