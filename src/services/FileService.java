package service;

import exceptions.ATMException;
import model.Account;
import model.Transaction;
import model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileService {
    private static final String HEADER = "ATM_SIMULATOR_V1";

    public HashMap<String, User> loadUsers(String filePath) throws ATMException {
        HashMap<String, User> users = new HashMap<>();
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            return users;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = readNextNonEmptyLine(reader);
            if (header == null) {
                return users;
            }

            if (!HEADER.equals(header)) {
                throw new ATMException("Некорректный формат файла данных");
            }

            String line;
            while ((line = readNextNonEmptyLine(reader)) != null) {
                if (!"USER".equals(line)) {
                    throw new ATMException("Ожидался блок USER в файле данных");
                }

                String cardNumber = readValue(reader, "card");
                String pin = readValue(reader, "pin");
                boolean blocked = Boolean.parseBoolean(readValue(reader, "blocked"));
                int failedAttempts = Integer.parseInt(readValue(reader, "failedAttempts"));
                double balance = Double.parseDouble(readValue(reader, "balance"));
                int transactionCount = Integer.parseInt(readValue(reader, "transactions"));

                User user = new User(cardNumber, pin, new Account(balance));
                user.setBlocked(blocked);
                user.setFailedAttempts(failedAttempts);

                for (int i = 0; i < transactionCount; i++) {
                    String transactionLine = readNextNonEmptyLine(reader);
                    if (transactionLine == null || !transactionLine.startsWith("tx=")) {
                        throw new ATMException("Некорректный формат транзакции в файле");
                    }

                    user.addTransaction(parseTransaction(transactionLine.substring(3)));
                }

                String endMarker = readNextNonEmptyLine(reader);
                if (!"END".equals(endMarker)) {
                    throw new ATMException("Ожидался маркер END в файле данных");
                }

                users.put(cardNumber, user);
            }
        } catch (IOException | NumberFormatException e) {
            throw new ATMException("Ошибка чтения файла: " + e.getMessage());
        }

        return users;
    }

    public void saveUsers(HashMap<String, User> users, String filePath) throws ATMException {
        Path path = Paths.get(filePath);

        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new ATMException("Не удалось подготовить директорию для файла: " + e.getMessage());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                java.nio.file.StandardOpenOption.WRITE)) {

            writer.write(HEADER);
            writer.newLine();

            List<String> cardNumbers = new ArrayList<>(users.keySet());
            Collections.sort(cardNumbers);

            for (String cardNumber : cardNumbers) {
                User user = users.get(cardNumber);
                if (user == null) {
                    continue;
                }

                writer.write("USER");
                writer.newLine();
                writer.write("card=" + user.getCardNumber());
                writer.newLine();
                writer.write("pin=" + user.getPin());
                writer.newLine();
                writer.write("blocked=" + user.isBlocked());
                writer.newLine();
                writer.write("failedAttempts=" + user.getFailedAttempts());
                writer.newLine();
                writer.write("balance=" + user.getAccount().getBalance());
                writer.newLine();

                List<Transaction> transactions = user.getTransactions();
                writer.write("transactions=" + transactions.size());
                writer.newLine();

                for (Transaction transaction : transactions) {
                    writer.write("tx=" + transaction.getType() + "|" + transaction.getAmount() + "|" + transaction.getDate());
                    writer.newLine();
                }

                writer.write("END");
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ATMException("Ошибка записи файла: " + e.getMessage());
        }
    }

    private String readNextNonEmptyLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
                line = line.substring(1);
            }

            if (!line.isEmpty()) {
                return line;
            }
        }
        return null;
    }

    private String readValue(BufferedReader reader, String key) throws IOException, ATMException {
        String line = readNextNonEmptyLine(reader);
        if (line == null) {
            throw new ATMException("Неожиданный конец файла данных");
        }

        String prefix = key + "=";
        if (!line.startsWith(prefix)) {
            throw new ATMException("Ожидалось поле " + key + " в файле данных");
        }

        return line.substring(prefix.length());
    }

    private Transaction parseTransaction(String rawValue) throws ATMException {
        String[] parts = rawValue.split("\\|", 3);
        if (parts.length != 3) {
            throw new ATMException("Некорректный формат транзакции");
        }

        try {
            return new Transaction(parts[0], Double.parseDouble(parts[1]), java.time.LocalDateTime.parse(parts[2]));
        } catch (Exception e) {
            throw new ATMException("Не удалось разобрать транзакцию: " + e.getMessage());
        }
    }
}


