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

class IpUtilsTest {

    private static final String FILENAME = "src/test/resources/isIPv4Address.json";

    @Test
    void isIPv4Address() throws IOException {

        Map<String, Boolean> map = new ObjectMapper().readValue(new File(FILENAME),
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
}