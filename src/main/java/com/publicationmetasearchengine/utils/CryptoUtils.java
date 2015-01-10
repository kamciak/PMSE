package com.publicationmetasearchengine.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public final class CryptoUtils {
    private static final Logger LOGGER = Logger.getLogger(CryptoUtils.class);

    private static Base64 coder;

    private static String password = "036sjbka+_//dw/^";
    private static SecretKey key;
    private static Cipher cipher;

    static {
        try {
            key = new SecretKeySpec(password.getBytes(), "AES");
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
            coder = new Base64(32, new byte[] {}, false);
        } catch (Throwable t) {
            LOGGER.fatal("CryptoUtils initialization failed", t);
            throw new RuntimeException(t);
        }
        LOGGER.info("Initialized");
    }

    public static synchronized String encrypt(String plainText) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherText = cipher.doFinal(plainText.getBytes());
            return new String(coder.encode(cipherText));
        } catch (Exception ex) {
            LOGGER.fatal("Encryption not possible", ex);
        }
        throw new RuntimeException();
    }

    public static synchronized String decrypt(String codedText) {
        try {
            byte[] encypted = coder.decode(codedText.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(encypted);
            return new String(decrypted);
        } catch (Exception ex) {
            LOGGER.fatal("Decryption not possible", ex);
        }
        throw new RuntimeException();
    }
}