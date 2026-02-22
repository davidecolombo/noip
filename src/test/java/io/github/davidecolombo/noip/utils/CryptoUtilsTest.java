package io.github.davidecolombo.noip.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilsTest {

    private static final String TEST_KEY = "TestEncryptionKey123";
    private static final String TEST_PASSWORD = "MySecretPassword";

    @Test
    void shouldEncryptPassword() {
        String encrypted = CryptoUtils.encrypt(TEST_PASSWORD, TEST_KEY);

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("ENC("));
        assertTrue(encrypted.endsWith(")"));
        assertNotEquals(TEST_PASSWORD, encrypted);
    }

    @Test
    void shouldDecryptPassword() {
        String encrypted = CryptoUtils.encrypt(TEST_PASSWORD, TEST_KEY);
        String decrypted = CryptoUtils.decrypt(encrypted, TEST_KEY);

        assertEquals(TEST_PASSWORD, decrypted);
    }

    @Test
    void shouldRoundTripEncryptDecrypt() {
        String[] passwords = {
            "simple",
            "complex!@#$%^&*()",
            "unicode: \u00E9\u00E0\u00FC",
            "numbers1234567890",
            "a",
            "verylongpasswordthatismuchlongerthanusualbutstillvalidpassword"
        };

        for (String password : passwords) {
            String encrypted = CryptoUtils.encrypt(password, TEST_KEY);
            String decrypted = CryptoUtils.decrypt(encrypted, TEST_KEY);
            assertEquals(password, decrypted, "Failed for: " + password);
        }
    }

    @Test
    void shouldDetectEncryptedValue() {
        String encrypted = CryptoUtils.encrypt(TEST_PASSWORD, TEST_KEY);
        assertTrue(CryptoUtils.isEncrypted(encrypted));
    }

    @Test
    void shouldDetectPlainTextValue() {
        assertFalse(CryptoUtils.isEncrypted(TEST_PASSWORD));
        assertFalse(CryptoUtils.isEncrypted("plaintext"));
        assertFalse(CryptoUtils.isEncrypted("ENC"));
        assertFalse(CryptoUtils.isEncrypted("ENC("));
        assertFalse(CryptoUtils.isEncrypted("ENC)"));
    }

    @Test
    void shouldHandleNullValues() {
        assertNull(CryptoUtils.encrypt(null, TEST_KEY));
        assertNull(CryptoUtils.decrypt(null, TEST_KEY));
        assertFalse(CryptoUtils.isEncrypted(null));
    }

    @Test
    void shouldHandleEmptyValues() {
        assertEquals("", CryptoUtils.encrypt("", TEST_KEY));
        assertEquals("", CryptoUtils.decrypt("", TEST_KEY));
        assertFalse(CryptoUtils.isEncrypted(""));
    }

    @Test
    void shouldFailDecryptWithWrongKey() {
        String encrypted = CryptoUtils.encrypt(TEST_PASSWORD, TEST_KEY);
        
        assertThrows(Exception.class, () -> {
            CryptoUtils.decrypt(encrypted, "WrongKey");
        });
    }

    @Test
    void shouldKeepPlainTextAsIsWhenDecrypting() {
        String plaintext = "JustAPlainText";
        String result = CryptoUtils.decrypt(plaintext, TEST_KEY);
        
        assertEquals(plaintext, result);
    }

    @Test
    void shouldTrimEncryptedValue() {
        String encrypted = CryptoUtils.encrypt(TEST_PASSWORD, TEST_KEY);
        String withSpaces = "  " + encrypted + "  ";
        
        String decrypted = CryptoUtils.decrypt(withSpaces, TEST_KEY);
        assertEquals(TEST_PASSWORD, decrypted);
    }

    @Test
    void shouldReturnFalseWhenCheckingForKeyIfNotSet() {
        assertFalse(CryptoUtils.hasEncryptionKey());
    }
}
