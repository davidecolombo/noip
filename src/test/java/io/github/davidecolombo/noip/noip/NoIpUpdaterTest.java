package io.github.davidecolombo.noip.noip;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import retrofit2.mock.Calls;
import io.github.davidecolombo.noip.TestUtils;
import io.github.davidecolombo.noip.exception.NoIpException;
import io.github.davidecolombo.noip.NoIpSettings;

import java.io.IOException;

@Slf4j
class NoIpUpdaterTest {

    @Test
    void shouldUpdateNoIpAddress() throws IOException {

        String status = "good";
        int exitCode = 0;

        try (MockedStatic<NoIpApiImpl> mockedApi = Mockito.mockStatic(NoIpApiImpl.class)) {

            INoIpApi api = Mockito.mock(INoIpApi.class);
            Mockito.when(api.update(
                    Mockito.anyString(), // hostname
                    Mockito.anyString() // ip
            )).thenReturn(Calls.response(
                    String.format("%s %s", status, TestUtils.LOOPBACK_ADDRESS)
            ));

            retrofit2.Retrofit retrofit = Mockito.mock(retrofit2.Retrofit.class);
            Mockito.when(retrofit.create(INoIpApi.class)).thenReturn(api);

            mockedApi.when(() -> NoIpApiImpl.create(
                    Mockito.anyString(), // username
                    Mockito.anyString(), // password
                    Mockito.anyString() // user-agent
            )).thenReturn(retrofit);

            Assertions.assertEquals(exitCode,
                    NoIpUpdater.update(
                            TestUtils.createMockedNoIpSettings(),
                            TestUtils.LOOPBACK_ADDRESS));
        }
    }

    @Test
    void shouldFailOnBadAuth() throws IOException {

        String status = "badauth";
        int exitCode = 3;

        try (MockedStatic<NoIpApiImpl> mockedApi = Mockito.mockStatic(NoIpApiImpl.class)) {

            INoIpApi api = Mockito.mock(INoIpApi.class);
            Mockito.when(api.update(
                    Mockito.anyString(), // hostname
                    Mockito.anyString() // ip
            )).thenReturn(Calls.response(status));

            retrofit2.Retrofit retrofit = Mockito.mock(retrofit2.Retrofit.class);
            Mockito.when(retrofit.create(INoIpApi.class)).thenReturn(api);

            mockedApi.when(() -> NoIpApiImpl.create(
                    Mockito.anyString(), // username
                    Mockito.anyString(), // password
                    Mockito.anyString() // user-agent
            )).thenReturn(retrofit);

            Assertions.assertEquals(exitCode,
                    NoIpUpdater.update(
                            TestUtils.createMockedNoIpSettings(),
                            TestUtils.LOOPBACK_ADDRESS));
        }
    }

    @Test
    void shouldThrowNoIpExceptionOnEmptyResponse() {
        try (MockedStatic<NoIpApiImpl> mockedApi = Mockito.mockStatic(NoIpApiImpl.class)) {

            INoIpApi api = Mockito.mock(INoIpApi.class);
            Mockito.when(api.update(
                    Mockito.anyString(), // hostname
                    Mockito.anyString() // ip
            )).thenReturn(Calls.response(StringUtils.EMPTY));

            retrofit2.Retrofit retrofit = Mockito.mock(retrofit2.Retrofit.class);
            Mockito.when(retrofit.create(INoIpApi.class)).thenReturn(api);

            mockedApi.when(() -> NoIpApiImpl.create(
                    Mockito.anyString(), // username
                    Mockito.anyString(), // password
                    Mockito.anyString() // user-agent
            )).thenReturn(retrofit);

            NoIpException exception = Assertions.assertThrows(NoIpException.class,
                    () -> NoIpUpdater.update(
                            TestUtils.createMockedNoIpSettings(),
                            TestUtils.LOOPBACK_ADDRESS));

            // The improved error handling wraps the original exception
            Assertions.assertEquals("Unexpected error during No-IP update", exception.getMessage());
            Assertions.assertTrue(exception.getCause().getMessage().contains("No-IP response is empty"));
        }
    }

    @Test
    void shouldReturnUnknownResponseExitCode() throws IOException {
        int exitCode = NoIpUpdater.ERROR_RETURN_CODE;
        try (MockedStatic<NoIpApiImpl> mockedApi = Mockito.mockStatic(NoIpApiImpl.class)) {

            INoIpApi api = Mockito.mock(INoIpApi.class);
            Mockito.when(api.update(
                    Mockito.anyString(), // hostname
                    Mockito.anyString() // ip
            )).thenReturn(Calls.response("this is not a response"));

            retrofit2.Retrofit retrofit = Mockito.mock(retrofit2.Retrofit.class);
            Mockito.when(retrofit.create(INoIpApi.class)).thenReturn(api);

            mockedApi.when(() -> NoIpApiImpl.create(
                    Mockito.anyString(), // username
                    Mockito.anyString(), // password
                    Mockito.anyString() // user-agent
            )).thenReturn(retrofit);

            Assertions.assertEquals(exitCode,
                    NoIpUpdater.update(
                            TestUtils.createMockedNoIpSettings(),
                            TestUtils.LOOPBACK_ADDRESS));
        }
    }

    @Test
    void shouldThrowNullPointerException() throws IOException {
        // Test null IP - should throw IllegalArgumentException (not wrapped)
        NoIpSettings validSettings = TestUtils.createMockedNoIpSettings();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> NoIpUpdater.update(validSettings, null));

        // Test null settings - should throw NoIpException (wrapped NullPointerException)
        NoIpException exception = Assertions.assertThrows(NoIpException.class,
                () -> NoIpUpdater.update(null, "127.0.0.1"));
        
        // The improved error handling wraps the original exception
        Assertions.assertEquals("Unexpected error during No-IP update", exception.getMessage());
        Assertions.assertTrue(exception.getCause() instanceof NullPointerException);
    }
}