import exceptions.ATMException;
import model.User;
import service.AuthService;
import service.FileService;
import service.ValidationService;
import ui.ConsoleMenu;

import java.util.HashMap;
import java.util.Scanner;

public class Main {
    private static final String DATA_FILE = "atm-users.txt";

    public static void main(String[] args) {
        FileService fileService = new FileService();
        HashMap<String, User> users;

        try {
            users = fileService.loadUsers(DATA_FILE);
        } catch (ATMException e) {
            System.out.println("Не удалось загрузить данные: " + e.getMessage());
            users = new HashMap<>();
        }

        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleMenu consoleMenu = new ConsoleMenu(
                    users,
                    new AuthService(),
                    new ValidationService(),
                    scanner
            );

            try {
                consoleMenu.start();
            } finally {
                try {
                    fileService.saveUsers(users, DATA_FILE);
                    System.out.println("Данные сохранены в файл: " + DATA_FILE);
                } catch (ATMException e) {
                    System.out.println("Не удалось сохранить данные: " + e.getMessage());
                }
            }
        }
    }
}

