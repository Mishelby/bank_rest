package com.example.bankcards.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MaskCardNumber  {
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
