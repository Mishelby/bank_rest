package com.example.bankcards.util;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;

public final class KeyStoreManager {
    private static final String KEY_ALIAS = "cardKey";
    private static final char[] KEYSTORE_PASSWORD = "changeit".toCharArray();
    private static final String KEY_PASSWORD = "changeit";
    private static final String KEYSTORE_FILE = "keystore.jks";

    private KeyStoreManager() {}

    public static SecretKey loadOrCreateKey() {
        try {
            KeyStore ks = KeyStore.getInstance("JCEKS");
            boolean exists = new java.io.File(KEYSTORE_FILE).exists();

            if (exists) {
                try (FileInputStream fis = new FileInputStream(KEYSTORE_FILE)) {
                    ks.load(fis, KEYSTORE_PASSWORD);
                }
            } else {
                ks.load(null, KEYSTORE_PASSWORD);
            }

            if (!ks.containsAlias(KEY_ALIAS)) {
                byte[] keyBytes = new byte[32];
                new java.security.SecureRandom().nextBytes(keyBytes);
                SecretKey key = new SecretKeySpec(keyBytes, "AES");
                KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(key);
                KeyStore.ProtectionParameter protParam =
                        new KeyStore.PasswordProtection(KEY_PASSWORD.toCharArray());
                ks.setEntry(KEY_ALIAS, entry, protParam);

                try (FileOutputStream fos = new FileOutputStream(KEYSTORE_FILE)) {
                    ks.store(fos, KEYSTORE_PASSWORD);
                }
                return key;
            } else {
                KeyStore.ProtectionParameter protParam =
                        new KeyStore.PasswordProtection(KEY_PASSWORD.toCharArray());
                KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) ks.getEntry(KEY_ALIAS, protParam);
                return entry.getSecretKey();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load/create key in keystore", e);
        }
    }
}
