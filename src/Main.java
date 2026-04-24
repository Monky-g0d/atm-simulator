import exceptions.ATMException;
import models.User;
import services.AuthService;
import services.FileService;
import services.ValidationService;
import ui.ConsoleMenu;

import java.util.HashMap;
import java.util.Scanner;

public class Main {
    private static final String DATA_FILE = "atm-users.txt";

    public static void main(String[] args) {
        FileService fileService = new FileService();
        AuthService authService = new AuthService();
        ValidationService validationService = new ValidationService();
        Scanner scanner = new Scanner(System.in);

        HashMap<String, User> users;

        try {
            // Загрузка данных при старте
            users = fileService.loadUsers(DATA_FILE);

            ConsoleMenu menu = new ConsoleMenu(users, authService, validationService, scanner);
            menu.start();

            // Сохранение данных при выходе
            fileService.saveUsers(users, DATA_FILE);
            System.out.println("Данные сохранены в файл: " + DATA_FILE);

        } catch (ATMException e) {
            System.out.println("Критическая ошибка системы: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}