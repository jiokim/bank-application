package com.bank.common.sensitive;

public interface TextEncryptor {

    String encrypt(String plainText);

    String decrypt(String cipherText);
}
