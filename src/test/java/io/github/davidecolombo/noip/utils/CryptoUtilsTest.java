package io.github.davidecolombo.noip.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CryptoUtils encryption/decryption functionality.
 * 
 * These tests verify the AES-256 encryption implementation:
 * - Password encryption produces encrypted format (ENC(...))
 * - Decryption with correct key recovers original password
 * - Round-trip encryption/decryption preserves data
 * - Encrypted vs plaintext detection works correctly
 * - Null and empty value handling
 * - Wrong key fails to decrypt
 */
class CryptoUtilsTest {

    private static final String TEST_KEY = "TestEncryptionKey123";
    private static final String TEST_PASSWORD = "MySecretPassword";

    /**
     * Tests that encryption produces the expected format.
     * 
     * The encrypted output should:
     * - Not be null
     * - Start with "ENC(" prefix
     * - End with ")" suffix
     * - Be different from the original password
     */
    @Test
    void shouldEncryptPassword() {
        String encrypted = CryptoUtils.encrypt(TEST_PASSWORD, TEST_KEY);

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("ENC("));
        assertTrue(encrypted.endsWith(")"));
        assertNotEquals(TEST_PASSWORD, encrypted);
    }

    /**
     * Tests that decryption with correct key recovers the original password.
     */
    @Test
    void shouldDecryptPassword() {
        String encrypted = CryptoUtils.encrypt(TEST_PASSWORD, TEST_KEY);
        String decrypted = CryptoUtils.decrypt(encrypted, TEST_KEY);

        assertEquals(TEST_PASSWORD, decrypted);
    }

    /**
     * Tests round-trip encryption/decryption with various password formats.
     * 
     * Verifies that different types of passwords (simple, complex, unicode,
     * short, long) can be encrypted and decrypted correctly.
     */
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

    /**
     * Tests that isEncrypted() correctly identifies encrypted values.
     * 
     * Values in ENC(...) format should be detected as encrypted.
     */
    @Test
    void shouldDetectEncryptedValue() {
        String encrypted = CryptoUtils.encrypt(TEST_PASSWORD, TEST_KEY);
        assertTrue(CryptoUtils.isEncrypted(encrypted));
    }

    /**
     * Tests that isEncrypted() correctly identifies plaintext values.
     * 
     * Plaintext values (including invalid ENC prefixes) should not be
     * detected as encrypted.
     */
    @Test
    void shouldDetectPlainTextValue() {
        assertFalse(CryptoUtils.isEncrypted(TEST_PASSWORD));
        assertFalse(CryptoUtils.isEncrypted("plaintext"));
        assertFalse(CryptoUtils.isEncrypted("ENC"));
        assertFalse(CryptoUtils.isEncrypted("ENC("));
        assertFalse(CryptoUtils.isEncrypted("ENC)"));
    }

    /**
     * Tests that null values are handled gracefully.
     * 
     * Encryption/decryption/isEncrypted should handle null inputs
     * without throwing exceptions.
     */
    @Test
    void shouldHandleNullValues() {
        assertNull(CryptoUtils.encrypt(null, TEST_KEY));
        assertNull(CryptoUtils.decrypt(null, TEST_KEY));
        assertFalse(CryptoUtils.isEncrypted(null));
    }

    /**
     * Tests that empty string values are handled gracefully.
     */
    @Test
    void shouldHandleEmptyValues() {
        assertEquals("", CryptoUtils.encrypt("", TEST_KEY));
        assertEquals("", CryptoUtils.decrypt("", TEST_KEY));
        assertFalse(CryptoUtils.isEncrypted(""));
    }

    /**
     * Tests that decryption fails when the wrong key is provided.
     * 
     * Using a different key than the one used for encryption should
     * throw an exception.
     */
    @Test
    void shouldFailDecryptWithWrongKey() {
        String encrypted = CryptoUtils.encrypt(TEST_PASSWORD, TEST_KEY);
        
        assertThrows(Exception.class, () -> {
            CryptoUtils.decrypt(encrypted, "WrongKey");
        });
    }

    /**
     * Tests that plaintext values are returned as-is when "decrypted".
     * 
     * The decrypt method should detect plaintext (non-ENC format) and
     * return it unchanged rather than attempting decryption.
     */
    @Test
    void shouldKeepPlainTextAsIsWhenDecrypting() {
        String plaintext = "JustAPlainText";
        String result = CryptoUtils.decrypt(plaintext, TEST_KEY);
        
        assertEquals(plaintext, result);
    }

    /**
     * Tests that encrypted values with surrounding whitespace are handled.
     * 
     * The decrypt method should trim whitespace from encrypted values
     * before processing.
     */
    @Test
    void shouldTrimEncryptedValue() {
        String encrypted = CryptoUtils.encrypt(TEST_PASSWORD, TEST_KEY);
        String withSpaces = "  " + encrypted + "  ";
        
        String decrypted = CryptoUtils.decrypt(withSpaces, TEST_KEY);
        assertEquals(TEST_PASSWORD, decrypted);
    }

    /**
     * Tests that hasEncryptionKey() returns false when no key is set.
     */
    @Test
    void shouldReturnFalseWhenCheckingForKeyIfNotSet() {
        assertFalse(CryptoUtils.hasEncryptionKey());
    }
}
