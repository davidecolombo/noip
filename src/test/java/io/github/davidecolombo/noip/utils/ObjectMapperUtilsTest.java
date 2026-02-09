package io.github.davidecolombo.noip.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ObjectMapperUtilsTest {

    @Test
    void shouldCreateObjectMapper() {
        ObjectMapper objectMapper = ObjectMapperUtils.createObjectMapper();
        Assertions.assertNotNull(objectMapper);
        Assertions.assertEquals(ObjectMapper.class, objectMapper.getClass());
    }
}