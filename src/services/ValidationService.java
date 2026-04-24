package service;

import exceptions.ATMException;

public class ValidationService {

    public String requireText(String value, String message) throws ATMException {
        if (value == null || value.trim().isEmpty()) {
            throw new ATMException(message);
        }

        return value.trim();
    }

    public String requireCardNumber(String value) throws ATMException {
        String cardNumber = requireText(value, "Введите номер карты");

        if (!cardNumber.matches("\\d{4,19}")) {
            throw new ATMException("Номер карты должен содержать от 4 до 19 цифр");
        }

        return cardNumber;
    }

    public String requirePin(String value) throws ATMException {
        String pin = requireText(value, "Введите PIN");

        if (!pin.matches("\\d{4}")) {
            throw new ATMException("PIN должен состоять из 4 цифр");
        }

        return pin;
    }

    public double parsePositiveAmount(String value) throws ATMException {
        String normalized = requireText(value, "Введите сумму").replace(',', '.');

        try {
            double amount = Double.parseDouble(normalized);
            if (amount <= 0) {
                throw new ATMException("Сумма должна быть больше нуля");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new ATMException("Некорректный формат суммы");
        }
    }
}

