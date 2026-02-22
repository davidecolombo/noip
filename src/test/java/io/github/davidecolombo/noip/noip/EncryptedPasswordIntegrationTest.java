package io.github.davidecolombo.noip.noip;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.davidecolombo.noip.NoIpSettings;
import io.github.davidecolombo.noip.utils.CryptoUtils;
import io.github.davidecolombo.noip.utils.ObjectMapperUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class EncryptedPasswordIntegrationTest {

    @BeforeEach
    void clearEnvVars() {
        System.clearProperty("noip.encryptor.key");
        System.clearProperty("NOIP_USERNAME");
        System.clearProperty("NOIP_PASSWORD");
        System.clearProperty("NOIP_HOSTNAME");
        System.clearProperty("NOIP_USER_AGENT");
    }

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

    @Test
    void shouldReadSettingsWithPlaintextPassword() throws IOException {
        System.clearProperty("noip.encryptor.key");
        
        ObjectMapper mapper = ObjectMapperUtils.createObjectMapper();
        File settingsFile = new File("src/test/resources/settings.json");
        
        NoIpSettings settings = mapper.readValue(settingsFile, NoIpSettings.class);
        
        String effectivePassword = settings.getPassword();
        
        org.junit.jupiter.api.Assertions.assertEquals("password", effectivePassword);
    }

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
