package com.example.bankcards.entity.enums.converter;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Converter(autoApply = true)
public class CardEncryptorConverter implements AttributeConverter<String, String> {

    private final SecretKey secretKey;
    private final IvParameterSpec iv;

    public CardEncryptorConverter(@Value("${card.encryption.key}") String secretKeyValue,
                                  @Value("${card.encryption.vector}") String initVector) {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyValue);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        this.iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String convertToDatabaseColumn(String number) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encryptedBytes = cipher.doFinal(number.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String number) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] decodedBytes = Base64.getDecoder().decode(number);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}



