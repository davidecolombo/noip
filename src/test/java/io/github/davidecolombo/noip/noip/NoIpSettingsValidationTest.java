package io.github.davidecolombo.noip.noip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.davidecolombo.noip.TestUtils;
import io.github.davidecolombo.noip.exception.ConfigurationException;
import io.github.davidecolombo.noip.NoIpSettings;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NoIpSettings validation logic.
 * 
 * These tests verify that the settings validation correctly identifies:
 * - Valid configurations (no exceptions thrown)
 * - Missing or empty required fields (username, hostname)
 * - Invalid user agent format
 */
class NoIpSettingsValidationTest {

    @BeforeEach
    void clearEnvVars() {
        System.clearProperty("NOIP_USERNAME");
        System.clearProperty("NOIP_PASSWORD");
        System.clearProperty("NOIP_HOSTNAME");
        System.clearProperty("NOIP_USER_AGENT");
    }

    /**
     * Tests that valid settings pass validation without throwing exceptions.
     * 
     * When all required fields (username, password, hostname) are present
     * and the user agent is valid, validation should succeed.
     */
    @Test
    void shouldValidateCorrectSettings() throws IOException {
        NoIpSettings settings = TestUtils.createMockedNoIpSettings();
        assertDoesNotThrow(() -> settings.validate());
    }

    /**
     * Tests that empty username is rejected during validation.
     * 
     * The username field is required and cannot be empty or null.
     * Validation should throw ConfigurationException with appropriate message.
     */
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

    /**
     * Tests that invalid user agent format is rejected.
     * 
     * User agent must follow No-IP's required format:
     * NameOfUpdateProgram/VersionNumber maintainercontact@domain.com
     */
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

    /**
     * Tests that empty hostname is rejected during validation.
     * 
     * The hostname field is required and cannot be empty or null.
     * Validation should throw ConfigurationException with appropriate message.
     */
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
