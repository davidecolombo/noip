package io.github.davidecolombo.noip.noip;

import org.junit.jupiter.api.Test;
import io.github.davidecolombo.noip.TestUtils;
import io.github.davidecolombo.noip.exception.ConfigurationException;
import io.github.davidecolombo.noip.NoIpSettings;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class NoIpSettingsValidationTest {

    @Test
    void shouldValidateCorrectSettings() throws IOException {
        NoIpSettings settings = TestUtils.createMockedNoIpSettings();
        assertDoesNotThrow(() -> settings.validate());
    }

    @Test
    void shouldThrowOnEmptyUserName() {
        NoIpSettings settings = new NoIpSettings();
        settings.setUserName("");
        settings.setPassword("password");
        settings.setHostName("hostname.example.com");
        settings.setUserAgent("TestApp/1.0 test@example.com");

        ConfigurationException exception = assertThrows(ConfigurationException.class,
                settings::validate);
        assertEquals("userName is required and cannot be empty", exception.getMessage());
    }

    @Test
    void shouldThrowOnInvalidUserAgent() {
        NoIpSettings settings = new NoIpSettings();
        settings.setUserName("username");
        settings.setPassword("password");
        settings.setHostName("hostname.example.com");
        settings.setUserAgent("invalid-user-agent");

        ConfigurationException exception = assertThrows(ConfigurationException.class,
                settings::validate);
        assertTrue(exception.getMessage().contains("userAgent 'invalid-user-agent' is invalid"));
    }

    @Test
    void shouldThrowOnEmptyHostName() {
        NoIpSettings settings = new NoIpSettings();
        settings.setUserName("username");
        settings.setPassword("password");
        settings.setHostName("");
        settings.setUserAgent("TestApp/1.0 test@example.com");

        ConfigurationException exception = assertThrows(ConfigurationException.class,
                settings::validate);
        assertEquals("hostName is required and cannot be empty", exception.getMessage());
    }
}