package com.example.bankcards.util;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class GenerateCardNumber {
    private static final int CARD_LENGTH = 16;

    public static String generateCardNumber() {
        var digits = new int[CARD_LENGTH];

        for (int i = 0; i < CARD_LENGTH - 1; i++) {
            digits[i] = ThreadLocalRandom.current().nextInt(10);
        }

        digits[CARD_LENGTH - 1] = calculateCheckDigit(digits);

        StringBuilder cardNumber = new StringBuilder();
        for (var i = 0; i < CARD_LENGTH; i++) {
            cardNumber.append(digits[i]);
            if (i < CARD_LENGTH - 1 && (i + 1) % 4 == 0) {
                cardNumber.append(" ");
            }
        }

        return cardNumber.toString();
    }

    private static int calculateCheckDigit(int[] digits) {
        var sum = 0;
        for (var i = 0; i < digits.length - 1; i++) {
            int digit = digits[digits.length - 2 - i];
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
        }
        return (10 - (sum % 10)) % 10;
    }
}