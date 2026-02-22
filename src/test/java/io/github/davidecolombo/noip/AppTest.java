package io.github.davidecolombo.noip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import retrofit2.mock.Calls;
import io.github.davidecolombo.noip.ipify.IpifyResponse;
import io.github.davidecolombo.noip.noip.INoIpApi;
import io.github.davidecolombo.noip.noip.NoIpApiImpl;
import io.github.davidecolombo.noip.utils.ObjectMapperUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
class AppTest {

    @BeforeEach
    void clearEnvVars() {
        System.clearProperty("NOIP_USERNAME");
        System.clearProperty("NOIP_PASSWORD");
        System.clearProperty("NOIP_HOSTNAME");
        System.clearProperty("NOIP_USER_AGENT");
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void testSystemExitWithStatus() throws IOException {

        try (MockedStatic<NoIpApiImpl> mockedApi = Mockito.mockStatic(NoIpApiImpl.class);
             MockedStatic<ObjectMapperUtils> mockedJsonUtils = Mockito.mockStatic(ObjectMapperUtils.class)) {

            retrofit2.Retrofit retrofit = Mockito.mock(retrofit2.Retrofit.class);
            INoIpApi api = Mockito.mock(INoIpApi.class);
            Mockito.when(retrofit.create(INoIpApi.class)).thenReturn(api);
            
            Mockito.when(api.update(
                    Mockito.anyString(), // hostname
                    Mockito.anyString() // ip
            )).thenReturn(Calls.response(String.format("%s %s", "nochg", TestUtils.LOOPBACK_ADDRESS)));

            mockedApi.when(() -> NoIpApiImpl.create(
                    Mockito.any(), // username
                    Mockito.any(), // password
                    Mockito.any() // user-agent
            )).thenReturn(retrofit);

            ObjectMapper objectMapper = Mockito.mock(
                    ObjectMapper.class,
                    Mockito.RETURNS_DEEP_STUBS);

            // fake settings - create a valid settings object that will pass validation
            NoIpSettings mockedSettings = TestUtils.createMockedNoIpSettings();
            Mockito.when(objectMapper.readValue(
                            Mockito.any(File.class),
                            Mockito.eq(NoIpSettings.class)))
                    .thenReturn(mockedSettings);

            // fake ipify
            IpifyResponse mockedIpifyResponse = new IpifyResponse();
            mockedIpifyResponse.setIp(TestUtils.LOOPBACK_ADDRESS);
            Mockito.when(objectMapper.readValue(
                            Mockito.any(URL.class),
                            Mockito.eq(IpifyResponse.class)))
                    .thenReturn(mockedIpifyResponse);

            mockedJsonUtils
                    .when(ObjectMapperUtils::createObjectMapper)
                    .thenReturn(objectMapper);
            Assertions.assertNotNull(ObjectMapperUtils.createObjectMapper());

            App.main(new String[]{"-settings", "dummy.json"});
            // wait for system exit with exit code
        }
    }

    @Test
    void shouldHandleNullArguments() {
        // The improved error handling now catches null args gracefully
        // and returns ERROR_RETURN_CODE (-1) instead of throwing
        Integer result = App.getInstance().run(null);
        Assertions.assertEquals(-1, result);
    }

    @Test
    @ExpectSystemExitWithStatus(2)
    void shouldThrowCmdLineException() {
        final String[] args = new String[] {};
        App.main(args);
    }
}
