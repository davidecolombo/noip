package io.github.davidecolombo.noip.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for IpUtils IP address validation.
 * 
 * Tests verify that the utility methods correctly identify:
 * - Valid IPv4 addresses
 * - Valid IPv6 addresses
 * - Invalid IP addresses
 * - Null inputs
 * 
 * Test data is loaded from JSON files:
 * - isIPv4Address.json: Valid/invalid IPv4 examples
 * - isIPv6Address.json: Valid/invalid IPv6 examples
 */
class IpUtilsTest {

    private static final String FILENAME_IPV4 = "src/test/resources/isIPv4Address.json";
    private static final String FILENAME_IPV6 = "src/test/resources/isIPv6Address.json";

    /**
     * Tests IPv4 address validation.
     * 
     * Loads test data from isIPv4Address.json and verifies that each
     * entry is correctly identified as valid or invalid IPv4.
     * Also verifies that null input returns false.
     */
    @Test
    void isIPv4Address() throws IOException {

        Map<String, Boolean> map = new ObjectMapper().readValue(new File(FILENAME_IPV4),
                new TypeReference<>() {
                });

        for (Entry<String, Boolean> entry : map.entrySet()) {
            String ip = entry.getKey();
            boolean expected = entry.getValue();
            boolean actual = IpUtils.isIPv4Address(ip);
            assertEquals(expected, actual);
        }

        Assertions.assertFalse(IpUtils.isIPv4Address(null));
    }

    /**
     * Tests IPv6 address validation.
     * 
     * Loads test data from isIPv6Address.json and verifies that each
     * entry is correctly identified as valid or invalid IPv6.
     * Also verifies that null input returns false.
     * 
     * IPv6 formats tested include:
     * - Full notation (2001:0db8:85a3:0000:0000:8a2e:0370:7334)
     * - Compressed notation (2001:db8::1)
     * - Loopback (::1)
     * - Link-local (fe80::1)
     * - With zone ID (fe80::1%eth0)
     */
    @Test
    void isIPv6Address() throws IOException {

        Map<String, Boolean> map = new ObjectMapper().readValue(new File(FILENAME_IPV6),
                new TypeReference<>() {
                });

        for (Entry<String, Boolean> entry : map.entrySet()) {
            String ip = entry.getKey();
            boolean expected = entry.getValue();
            boolean actual = IpUtils.isIPv6Address(ip);
            assertEquals(expected, actual);
        }

        Assertions.assertFalse(IpUtils.isIPv6Address(null));
    }
}