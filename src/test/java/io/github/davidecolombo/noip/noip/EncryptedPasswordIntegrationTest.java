package io.github.davidecolombo.noip.noip;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.davidecolombo.noip.NoIpSettings;
import io.github.davidecolombo.noip.utils.CryptoUtils;
import io.github.davidecolombo.noip.utils.ObjectMapperUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
 * Integration tests for encrypted password handling.
 * 
 * These tests verify that the application correctly handles both
 * encrypted and plaintext passwords in the settings file:
 * - Decrypts encrypted passwords using the provided key
 * - Preserves plaintext passwords as-is
 * - Fails gracefully when decryption key is wrong
 */
class EncryptedPasswordIntegrationTest {

    @BeforeEach
    void clearEnvVars() {
        System.clearProperty("noip.encryptor.key");
        System.clearProperty("NOIP_USERNAME");
        System.clearProperty("NOIP_PASSWORD");
        System.clearProperty("NOIP_HOSTNAME");
        System.clearProperty("NOIP_USER_AGENT");
    }

    /**
     * Tests that encrypted passwords are correctly decrypted when loaded.
     * 
     * When the encryption key is provided via system property and the settings
     * file contains an encrypted password (ENC(...)), the password should be
     * decrypted and available in plain text.
     */
    @Test
    void shouldReadSettingsWithEncryptedPassword() throws IOException {
        System.setProperty("noip.encryptor.key", "TestKey123");
        
        try {
            ObjectMapper mapper = ObjectMapperUtils.createObjectMapper();
            File settingsFile = new File("src/test/resources/settings-encrypted.json");
            
            NoIpSettings settings = mapper.readValue(settingsFile, NoIpSettings.class);
            
            String effectivePassword = settings.getPassword();
            
            org.junit.jupiter.api.Assertions.assertEquals("TestPassword123", settings.getPassword());
        } finally {
            System.clearProperty("noip.encryptor.key");
        }
    }

    /**
     * Tests that plaintext passwords are preserved when no encryption key is set.
     * 
     * When the settings file contains a plaintext password and no encryption
     * key is provided, the password should be returned as-is.
     */
    @Test
    void shouldReadSettingsWithPlaintextPassword() throws IOException {
        System.clearProperty("noip.encryptor.key");
        
        ObjectMapper mapper = ObjectMapperUtils.createObjectMapper();
        File settingsFile = new File("src/test/resources/settings.json");
        
        NoIpSettings settings = mapper.readValue(settingsFile, NoIpSettings.class);
        
        String effectivePassword = settings.getPassword();
        
        org.junit.jupiter.api.Assertions.assertEquals("password", effectivePassword);
    }

    /**
     * Tests that decryption fails when the wrong key is provided.
     * 
     * When the encryption key doesn't match the one used to encrypt the
     * password, attempting to get the password should throw an exception.
     */
    @Test
    void shouldFailToDecryptWithWrongKey() throws IOException {
        System.setProperty("noip.encryptor.key", "WrongKey");
        
        try {
            ObjectMapper mapper = ObjectMapperUtils.createObjectMapper();
            File settingsFile = new File("src/test/resources/settings-encrypted.json");
            
            NoIpSettings settings = mapper.readValue(settingsFile, NoIpSettings.class);
            
            org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
                settings.getPassword();
            });
        } finally {
            System.clearProperty("noip.encryptor.key");
        }
    }
}
