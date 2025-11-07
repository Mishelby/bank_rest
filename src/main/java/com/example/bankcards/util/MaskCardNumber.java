package com.example.bankcards.util;

import lombok.experimental.UtilityClass;


/**
 * Утилитный класс для маскирования номеров банковских карт.
 * <p>
 * Предназначен для безопасного отображения номера карты, скрывая все цифры,
 * кроме последних четырёх. Также добавляет пробелы каждые 4 символа
 * для удобства чтения.
 * </p>
 *
 * <p><b>Особенности:</b></p>
 * <ul>
 *   <li>Допускаются номера длиной от 16 до 19 цифр.</li>
 *   <li>Пробелы и другие нецифровые символы в исходной строке игнорируются.</li>
 *   <li>Если входное значение {@code null} — выбрасывается {@link RuntimeException}.</li>
 *   <li>Если длина номера некорректна — выбрасывается {@link RuntimeException}.</li>
 * </ul>
 */
@UtilityClass
public class MaskCardNumber  {

    /**
     * Маскирует номер карты, оставляя видимыми только последние 4 цифры.
     * <p>
     * Пример:
     * <ul>
     *   <li>Вход: {@code "1234 5678 9012 3456"}</li>
     *   <li>Выход: {@code "**** **** **** 3456"}</li>
     * </ul>
     * </p>
     *
     * @param cardNumber номер карты (может содержать пробелы или другие символы)
     * @return замаскированный номер карты в формате с пробелами каждые 4 цифры
     * @throws RuntimeException если номер карты {@code null} или его длина некорректна
     */
    public String mask(String cardNumber) {
        if (cardNumber == null) {
            throw new RuntimeException("Card number is null");
        }

        String digitsOnly = cardNumber.replaceAll("\\D", "");

        if (digitsOnly.length() < 16 || digitsOnly.length() > 19) {
            throw new RuntimeException("Invalid card number");
        }

        String maskedDigits = digitsOnly.replaceAll("\\d(?=\\d{4})", "*");

        return maskedDigits.replaceAll("(.{4})(?=.)", "$1 ");
    }
}
