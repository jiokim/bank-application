package com.bank.common.sensitive;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public class AesGcmTextEncryptor implements TextEncryptor {

    private static final String PREFIX = "ENC(";
    private static final String SUFFIX = ")";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom;

    public AesGcmTextEncryptor(String base64Key) {
        this(base64Key, new SecureRandom());
    }

    AesGcmTextEncryptor(String base64Key, SecureRandom secureRandom) {
        byte[] key = Base64.getDecoder().decode(base64Key);
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException("AES key must be 16, 24, or 32 bytes after Base64 decoding.");
        }
        this.keySpec = new SecretKeySpec(key, "AES");
        this.secureRandom = secureRandom;
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || isEncrypted(plainText)) {
            return plainText;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            byte[] encrypted = cipher.doFinal(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return PREFIX + Base64.getEncoder().encodeToString(payload) + SUFFIX;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt sensitive value.", e);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        if (cipherText == null || !isEncrypted(cipherText)) {
            return cipherText;
        }
        try {
            String encoded = cipherText.substring(PREFIX.length(), cipherText.length() - SUFFIX.length());
            byte[] payload = Base64.getDecoder().decode(encoded);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(payload, 0, iv, 0, IV_LENGTH);

            byte[] encrypted = new byte[payload.length - IV_LENGTH];
            System.arraycopy(payload, IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            return new String(cipher.doFinal(encrypted), java.nio.charset.StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to decrypt sensitive value.", e);
        }
    }

    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX) && value.endsWith(SUFFIX);
    }
}
