package com.example.bankcards.entity.enums.converter;

import com.example.bankcards.util.AesGcmEncryptor;
import com.example.bankcards.util.KeyStoreManager;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import javax.crypto.SecretKey;

@Converter
public class CardEncryptorConverter implements AttributeConverter<String, String> {
    private static final AesGcmEncryptor encryptor;

    static {
        SecretKey key = KeyStoreManager.loadOrCreateKey();
        encryptor = new AesGcmEncryptor(key);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;

        String normalized = attribute.replaceAll("\\s+", "");
        if (!normalized.matches("\\d{16}")) {
            throw new IllegalArgumentException("Card number must be 16 digits");
        }

        return encryptor.encrypt(normalized);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        String decrypted = encryptor.decrypt(dbData);
        return formatWithSpaces(decrypted);
    }

    private String formatWithSpaces(String digits) {
        return String.format("%s %s %s %s",
                digits.substring(0, 4),
                digits.substring(4, 8),
                digits.substring(8, 12),
                digits.substring(12, 16));
    }
}

