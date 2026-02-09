package io.github.davidecolombo.noip;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import io.github.davidecolombo.noip.NoIpSettings;

import java.io.File;
import java.io.IOException;

@UtilityClass
public class TestUtils {

    public static final String LOOPBACK_ADDRESS = "127.0.0.1";

    public static NoIpSettings createMockedNoIpSettings() throws IOException {
        NoIpSettings settings = new NoIpSettings();
        settings.setUserName("username");
        settings.setPassword("password");
        settings.setHostName("hostname.example.com");
        settings.setUserAgent("TestApp/1.0 test@example.com");
        new ObjectMapper()
                .readerForUpdating(settings)
                .readValue(new File(System.getProperty("user.dir")
                        + "/src/main/resources/responses.json"));
        return settings;
    }
}