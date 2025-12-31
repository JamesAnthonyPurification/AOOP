package com.backup.utils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class CryptoUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public static void encrypt(File inputFile, File outputFile, String key) throws IOException, GeneralSecurityException {
        doCrypto(Cipher.ENCRYPT_MODE, inputFile, outputFile, key);
    }

    public static void decrypt(File inputFile, File outputFile, String key) throws IOException, GeneralSecurityException {
        doCrypto(Cipher.DECRYPT_MODE, inputFile, outputFile, key);
    }

    private static void doCrypto(int cipherMode, File inputFile, File outputFile, String keyStr) throws IOException, GeneralSecurityException {
        // Pad key to 16 bytes (128 bit)
        byte[] keyBytes = Arrays.copyOf(keyStr.getBytes("UTF-8"), 16);
        SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(cipherMode, secretKey);

        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            
            if (cipherMode == Cipher.ENCRYPT_MODE) {
                try (CipherOutputStream cos = new CipherOutputStream(outputStream, cipher)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        cos.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                try (CipherInputStream cis = new CipherInputStream(inputStream, cipher)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = cis.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }
}
