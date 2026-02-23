package io.github.davidecolombo.noip.noip;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.davidecolombo.noip.NoIpSettings;
import io.github.davidecolombo.noip.exception.NoIpException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Integration tests for NoIpUpdater class with MockWebServer.
 * 
 * These tests verify the complete NoIpUpdater.update() workflow using
 * a mock HTTP server to simulate real No-IP API responses:
 * 
 * Test scenarios:
 * - Full IPv4 update workflow (good response -> exit code 0)
 * - Full IPv6 update workflow (good response -> exit code 0)
 * - IP unchanged scenario (nochg response -> exit code 1)
 * - Authentication failure (badauth -> exit code 3)
 * - Invalid hostname (nohost -> exit code 2)
 * - IPv4 protocol rejects IPv6 addresses (IllegalArgumentException)
 * - IPv6 protocol rejects IPv4 addresses (IllegalArgumentException)
 * - Network errors (NoIpException)
 */
@Slf4j
class NoIpUpdaterIntegrationTest {

    private MockWebServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockServer != null) {
            mockServer.shutdown();
        }
    }

    private NoIpSettings createSettings(String ipProtocol) {
        NoIpSettings settings = new NoIpSettings();
        settings.setUserName("testuser");
        settings.setPassword("testpass");
        settings.setHostName("hostname.example.com");
        settings.setUserAgent("TestApp/1.0 test@test.com");
        if (ipProtocol != null) {
            settings.setIpProtocol(NoIpSettings.IpProtocol.valueOf(ipProtocol));
        }
        
        try {
            new ObjectMapper()
                    .readerForUpdating(settings)
                    .readValue(new File("src/main/resources/responses.json"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load responses", e);
        }
        
        return settings;
    }

    /**
     * Tests complete IPv4 update workflow.
     * 
     * Simulates: No-IP returns "good 192.168.1.100" for an IPv4 address.
     * Expected: Exit code 0 (successful update)
     */
    @Test
    void fullUpdateWorkflow_ipv4() throws Exception {
        String ipAddress = "192.168.1.100";
        
        mockServer.enqueue(new MockResponse()
                .setBody(String.format("good %s", ipAddress))
                .addHeader("Content-Type", "text/plain"));

        OkHttpClient httpClient = createMockClient();
        
        Retrofit retrofit = createRetrofit(mockServer.url("/").toString(), httpClient);
        INoIpApi api = retrofit.create(INoIpApi.class);

        NoIpSettings settings = createSettings("DUAL");
        
        try (var mockedApi = org.mockito.Mockito.mockStatic(NoIpApiImpl.class)) {
            mockedApi.when(() -> NoIpApiImpl.create(
                    org.mockito.Mockito.anyString(),
                    org.mockito.Mockito.anyString(),
                    org.mockito.Mockito.anyString()
            )).thenReturn(retrofit);

            Integer exitCode = io.github.davidecolombo.noip.noip.NoIpUpdater.update(settings, ipAddress);
            Assertions.assertEquals(0, exitCode);
        }
    }

    /**
     * Tests complete IPv6 update workflow.
     * 
     * Simulates: No-IP returns "good 2001:db8::1" for an IPv6 address.
     * Expected: Exit code 0 (successful update)
     */
    @Test
    void fullUpdateWorkflow_ipv6() throws Exception {
        String ipAddress = "2001:db8::1";
        
        mockServer.enqueue(new MockResponse()
                .setBody(String.format("good %s", ipAddress))
                .addHeader("Content-Type", "text/plain"));

        OkHttpClient httpClient = createMockClient();
        
        Retrofit retrofit = createRetrofit(mockServer.url("/").toString(), httpClient);
        INoIpApi api = retrofit.create(INoIpApi.class);

        NoIpSettings settings = createSettings("DUAL");
        
        try (var mockedApi = org.mockito.Mockito.mockStatic(NoIpApiImpl.class)) {
            mockedApi.when(() -> NoIpApiImpl.create(
                    org.mockito.Mockito.anyString(),
                    org.mockito.Mockito.anyString(),
                    org.mockito.Mockito.anyString()
            )).thenReturn(retrofit);

            Integer exitCode = io.github.davidecolombo.noip.noip.NoIpUpdater.update(settings, ipAddress);
            Assertions.assertEquals(0, exitCode);
        }
    }

    /**
     * Tests when IP address is already up to date.
     * 
     * Simulates: No-IP returns "nochg 1.2.3.4" (IP unchanged).
     * Expected: Exit code 1 (no change needed)
     */
    @Test
    void ipUnchanged_nochg() throws Exception {
        String ipAddress = "1.2.3.4";
        
        mockServer.enqueue(new MockResponse()
                .setBody(String.format("nochg %s", ipAddress))
                .addHeader("Content-Type", "text/plain"));

        OkHttpClient httpClient = createMockClient();
        
        Retrofit retrofit = createRetrofit(mockServer.url("/").toString(), httpClient);

        NoIpSettings settings = createSettings("DUAL");
        
        try (var mockedApi = org.mockito.Mockito.mockStatic(NoIpApiImpl.class)) {
            mockedApi.when(() -> NoIpApiImpl.create(
                    org.mockito.Mockito.anyString(),
                    org.mockito.Mockito.anyString(),
                    org.mockito.Mockito.anyString()
            )).thenReturn(retrofit);

            Integer exitCode = io.github.davidecolombo.noip.noip.NoIpUpdater.update(settings, ipAddress);
            Assertions.assertEquals(1, exitCode);
        }
    }

    /**
     * Tests authentication failure handling.
     * 
     * Simulates: No-IP returns "badauth" (invalid credentials).
     * Expected: Exit code 3 (authentication failed)
     */
    @Test
    void badAuthentication() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("badauth")
                .addHeader("Content-Type", "text/plain"));

        OkHttpClient httpClient = createMockClient();
        
        Retrofit retrofit = createRetrofit(mockServer.url("/").toString(), httpClient);

        NoIpSettings settings = createSettings("DUAL");
        
        try (var mockedApi = org.mockito.Mockito.mockStatic(NoIpApiImpl.class)) {
            mockedApi.when(() -> NoIpApiImpl.create(
                    org.mockito.Mockito.anyString(),
                    org.mockito.Mockito.anyString(),
                    org.mockito.Mockito.anyString()
            )).thenReturn(retrofit);

            Integer exitCode = io.github.davidecolombo.noip.noip.NoIpUpdater.update(settings, "1.2.3.4");
            Assertions.assertEquals(3, exitCode);
        }
    }

    /**
     * Tests invalid hostname handling.
     * 
     * Simulates: No-IP returns "nohost" (hostname doesn't exist).
     * Expected: Exit code 2 (invalid hostname)
     */
    @Test
    void invalidHostname() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("nohost")
                .addHeader("Content-Type", "text/plain"));

        OkHttpClient httpClient = createMockClient();
        
        Retrofit retrofit = createRetrofit(mockServer.url("/").toString(), httpClient);

        NoIpSettings settings = createSettings("DUAL");
        
        try (var mockedApi = org.mockito.Mockito.mockStatic(NoIpApiImpl.class)) {
            mockedApi.when(() -> NoIpApiImpl.create(
                    org.mockito.Mockito.anyString(),
                    org.mockito.Mockito.anyString(),
                    org.mockito.Mockito.anyString()
            )).thenReturn(retrofit);

            Integer exitCode = io.github.davidecolombo.noip.noip.NoIpUpdater.update(settings, "1.2.3.4");
            Assertions.assertEquals(2, exitCode);
        }
    }

    /**
     * Tests that IPv4 protocol setting rejects IPv6 addresses.
     * 
     * When ipProtocol is set to IPV4, attempting to update with an IPv6
     * address should throw IllegalArgumentException.
     */
    @Test
    void ipv4ProtocolRejectsIPv6() {
        NoIpSettings settings = createSettings("IPV4");
        
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> io.github.davidecolombo.noip.noip.NoIpUpdater.update(settings, "2001:db8::1")
        );
        
        Assertions.assertTrue(exception.getMessage().contains("is not a valid address for protocol"));
    }

    /**
     * Tests that IPv6 protocol setting rejects IPv4 addresses.
     * 
     * When ipProtocol is set to IPV6, attempting to update with an IPv4
     * address should throw IllegalArgumentException.
     */
    @Test
    void ipv6ProtocolRejectsIPv4() {
        NoIpSettings settings = createSettings("IPV6");
        
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> io.github.davidecolombo.noip.noip.NoIpUpdater.update(settings, "192.168.1.1")
        );
        
        Assertions.assertTrue(exception.getMessage().contains("is not a valid address for protocol"));
    }

    /**
     * Tests network error handling.
     * 
     * When the No-IP server is unreachable, the updater should throw
     * NoIpException with the original IOException as the cause.
     */
    @Test
    void networkError_ipify() {
        NoIpSettings settings = createSettings("DUAL");
        
        // Use an unreachable base URL to simulate network error
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.MILLISECONDS)
                .readTimeout(100, TimeUnit.MILLISECONDS)
                .build();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://127.0.0.1:1/")  // Invalid port
                .addConverterFactory(retrofit2.converter.scalars.ScalarsConverterFactory.create())
                .client(httpClient)
                .build();
        
        try (var mockedApi = org.mockito.Mockito.mockStatic(NoIpApiImpl.class)) {
            mockedApi.when(() -> NoIpApiImpl.create(
                    org.mockito.Mockito.anyString(),
                    org.mockito.Mockito.anyString(),
                    org.mockito.Mockito.anyString()
            )).thenReturn(retrofit);
            
            NoIpException exception = Assertions.assertThrows(
                    NoIpException.class,
                    () -> io.github.davidecolombo.noip.noip.NoIpUpdater.update(settings, "1.2.3.4")
            );
            
            Assertions.assertNotNull(exception.getCause());
        }
    }

    private OkHttpClient createMockClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(new BasicAuthInterceptor("testuser", "testpass"))
                .addInterceptor(new UserAgentInterceptor("TestApp/1.0 test@test.com"))
                .build();
    }

    private Retrofit createRetrofit(String baseUrl, OkHttpClient httpClient) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(retrofit2.converter.scalars.ScalarsConverterFactory.create())
                .client(httpClient)
                .build();
    }

    private static class BasicAuthInterceptor implements Interceptor {
        private final String username;
        private final String password;

        BasicAuthInterceptor(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request.Builder builder = originalRequest.newBuilder()
                    .header("Authorization", Credentials.basic(username, password));
            return chain.proceed(builder.build());
        }
    }

    private static class UserAgentInterceptor implements Interceptor {
        private final String userAgent;

        UserAgentInterceptor(String userAgent) {
            this.userAgent = userAgent;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request.Builder builder = originalRequest.newBuilder()
                    .header("User-Agent", userAgent);
            return chain.proceed(builder.build());
        }
    }
}
