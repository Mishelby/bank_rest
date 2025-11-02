package com.example.bankcards.util;

import lombok.experimental.UtilityClass;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@UtilityClass
public class GenerateCardNumber {

    private static final Set<String> generatedCards = new HashSet<>();
    private static final Random random = new Random();

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
