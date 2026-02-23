package io.github.davidecolombo.noip.noip;

import io.github.davidecolombo.noip.NoIpSettings;
import io.github.davidecolombo.noip.TestUtils;
import io.github.davidecolombo.noip.exception.NoIpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import retrofit2.mock.Calls;

import java.io.IOException;

/**
 * Unit tests for NoIpUpdater class.
 * 
 * These tests verify the NoIpUpdater.update() method handles:
 * - Successful DNS updates (good response -> exit code 0)
 * - Authentication failures (badauth -> exit code 3)
 * - Empty responses (throws NoIpException)
 * - Unknown responses (returns ERROR_RETURN_CODE)
 * - Null input validation (throws IllegalArgumentException)
 */
@Slf4j
class NoIpUpdaterTest {

    /**
     * Tests successful DNS update when No-IP returns "good" response.
     * 
     * When No-IP successfully updates the DNS record, it returns "good".
     * The updater should map this to exit code 0.
     */
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
                    io.github.davidecolombo.noip.noip.NoIpUpdater.update(
                            TestUtils.createMockedNoIpSettings(),
                            TestUtils.LOOPBACK_ADDRESS));
        }
    }

    /**
     * Tests that authentication failures are properly handled.
     * 
     * When No-IP returns "badauth", the updater should map this to
     * exit code 3 (authentication failed).
     */
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
                    io.github.davidecolombo.noip.noip.NoIpUpdater.update(
                            TestUtils.createMockedNoIpSettings(),
                            TestUtils.LOOPBACK_ADDRESS));
        }
    }

    /**
     * Tests that empty responses from No-IP throw an exception.
     * 
     * When the No-IP API returns an empty body, this indicates an error
     * and should result in a NoIpException.
     */
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
                    () -> io.github.davidecolombo.noip.noip.NoIpUpdater.update(
                            TestUtils.createMockedNoIpSettings(),
                            TestUtils.LOOPBACK_ADDRESS));

            // The improved error handling wraps the original exception
            Assertions.assertEquals("Unexpected error during No-IP update", exception.getMessage());
            Assertions.assertTrue(exception.getCause().getMessage().contains("No-IP response is empty"));
        }
    }

    /**
     * Tests that unknown No-IP responses are handled gracefully.
     * 
     * When No-IP returns a response that doesn't match any known status,
     * the updater should return ERROR_RETURN_CODE (-1).
     */
    @Test
    void shouldReturnUnknownResponseExitCode() throws IOException {
        int exitCode = io.github.davidecolombo.noip.noip.NoIpUpdater.ERROR_RETURN_CODE;
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
                    io.github.davidecolombo.noip.noip.NoIpUpdater.update(
                            TestUtils.createMockedNoIpSettings(),
                            TestUtils.LOOPBACK_ADDRESS));
        }
    }

    /**
     * Tests null input validation.
     * 
     * - Null IP should throw IllegalArgumentException (not wrapped)
     * - Null settings should throw NoIpException (wrapped NullPointerException)
     */
    @Test
    void shouldThrowNullPointerException() throws IOException {
        // Test null IP - should throw IllegalArgumentException (not wrapped)
        NoIpSettings validSettings = TestUtils.createMockedNoIpSettings();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> io.github.davidecolombo.noip.noip.NoIpUpdater.update(validSettings, null));

        // Test null settings - should throw NoIpException (wrapped NullPointerException)
        NoIpException exception = Assertions.assertThrows(NoIpException.class,
                () -> io.github.davidecolombo.noip.noip.NoIpUpdater.update(null, "127.0.0.1"));
        
        // The improved error handling wraps the original exception
        Assertions.assertEquals("Unexpected error during No-IP update", exception.getMessage());
        Assertions.assertTrue(exception.getCause() instanceof NullPointerException);
    }
}