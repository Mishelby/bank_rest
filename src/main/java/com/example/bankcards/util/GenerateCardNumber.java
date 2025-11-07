package com.example.bankcards.util;

import lombok.experimental.UtilityClass;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
/**
 * Утилитный класс для генерации уникальных номеров карт, соответствующих алгоритму Луна (Luhn Algorithm).
 * <p>
 * Генерируемые номера состоят из 16 цифр и форматируются группами по 4 цифры
 * (например: "1234 5678 9012 3456").
 * </p>
 *
 * <p>
 * Класс также гарантирует уникальность номеров в рамках текущего запуска приложения —
 * уже сгенерированные номера сохраняются в памяти и не могут повториться.
 * </p>
 *
 * <p><b>Важно:</b> данный класс не является потокобезопасным.
 * Кроме того, сохранение всех номеров в памяти может привести к росту потребления памяти
 * при большом количестве генераций.</p>
 */
@UtilityClass
public class GenerateCardNumber {
    /**
     * Набор уже сгенерированных номеров карт, чтобы исключить дубликаты.
     */
    private static final Set<String> generatedCards = new HashSet<>();
    /**
     * Генератор случайных чисел для создания номеров карт.
     */
    private static final Random random = new Random();

    /**
     * Генерирует уникальный номер карты, соответствующий алгоритму Луна.
     * <p>
     * Формат выходного значения: 16 цифр, разделённых пробелами каждые 4 символа.
     * Пример: "1234 5678 9012 3456".
     * </p>
     *
     * @return уникальный, корректный по алгоритму Луна номер карты в отформатированном виде
     */
    public static String generateCardNumber() {
        String cardNumber;

        do {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 15; i++) {
                builder.append(random.nextInt(10));
            }

            int checkDigit = getLuhnCheckDigit(builder.toString());
            builder.append(checkDigit);

            cardNumber = builder.toString();
        } while (generatedCards.contains(cardNumber));

        generatedCards.add(cardNumber);

        return formatCardNumber(cardNumber);
    }

    /**
     * Вычисляет контрольную цифру по алгоритму Луна (Luhn Check Digit).
     * <p>
     * Алгоритм используется для проверки корректности номера карты.
     * </p>
     *
     * @param numberWithoutCheckDigit номер карты без последней (контрольной) цифры
     * @return контрольная цифра, которую нужно добавить в конец номера
     */
    private static int getLuhnCheckDigit(String numberWithoutCheckDigit) {
        int sum = 0;
        boolean alternate = true;
        for (int i = numberWithoutCheckDigit.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(numberWithoutCheckDigit.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Форматирует номер карты, добавляя пробелы каждые 4 символа.
     * <p>
     * Например, из "1234567890123456" получится "1234 5678 9012 3456".
     * </p>
     *
     * @param rawNumber необработанный номер карты (только цифры)
     * @return отформатированный номер карты
     */
    private static String formatCardNumber(String rawNumber) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < rawNumber.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(rawNumber.charAt(i));
        }
        return formatted.toString();
    }
}
