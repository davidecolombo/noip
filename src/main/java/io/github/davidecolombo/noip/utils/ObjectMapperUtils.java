package io.github.davidecolombo.noip.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectMapperUtils {

    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }
}