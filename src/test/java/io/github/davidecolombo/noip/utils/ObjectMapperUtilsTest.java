package io.github.davidecolombo.noip.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for ObjectMapperUtils factory class.
 * 
 * Verifies that the utility correctly creates configured
 * Jackson ObjectMapper instances.
 */
class ObjectMapperUtilsTest {

    /**
     * Tests that createObjectMapper returns a valid ObjectMapper instance.
     */
    @Test
    void shouldCreateObjectMapper() {
        ObjectMapper objectMapper = ObjectMapperUtils.createObjectMapper();
        Assertions.assertNotNull(objectMapper);
        Assertions.assertEquals(ObjectMapper.class, objectMapper.getClass());
    }
}