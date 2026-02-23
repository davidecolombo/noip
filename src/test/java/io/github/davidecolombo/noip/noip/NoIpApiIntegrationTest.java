package io.github.davidecolombo.noip.noip;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Interceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Integration tests for the No-IP API client using MockWebServer.
 * 
 * These tests verify that the INoIpApi interface correctly handles various
 * No-IP server responses. The tests use a mock HTTP server to simulate
 * real No-IP API responses without making actual network calls.
 * 
 * Test scenarios covered:
 * - Successful DNS updates (good, nochg responses)
 * - Authentication failures (badauth)
 * - Invalid hostnames (nohost)
 * - Account abuse detection (abuse)
 * - Server errors (HTTP 500)
 */
class NoIpApiIntegrationTest {

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

    /**
     * Tests that the API correctly handles a successful DNS update response.
     * 
     * Verifies:
     * - Response body is not null
     * - Response starts with "good" status
     * - The IP address in the response matches the requested IP
     */
    @Test
    void shouldHandleGoodResponse() throws IOException {
        String ipAddress = "192.168.1.100";
        
        mockServer.enqueue(new MockResponse()
                .setBody(String.format("good %s", ipAddress))
                .addHeader("Content-Type", "text/plain"));

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(new BasicAuthInterceptor("testuser", "testpass"))
                .addInterceptor(new UserAgentInterceptor("TestApp/1.0 test@test.com"))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mockServer.url("/"))
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient)
                .build();

        INoIpApi api = retrofit.create(INoIpApi.class);
        
        retrofit2.Response<String> response = api.update("hostname.example.com", ipAddress).execute();
        String body = response.body();

        Assertions.assertNotNull(body);
        Assertions.assertTrue(body.startsWith("good"));
        Assertions.assertTrue(body.contains(ipAddress));
    }

    /**
     * Tests that the API correctly handles a "no change" response.
     * 
     * When the IP address is already up to date, No-IP returns "nochg".
     * This test verifies the response is properly parsed and recognized.
     */
    @Test
    void shouldHandleNochgResponse() throws IOException {
        String ipAddress = "192.168.1.100";
        
        mockServer.enqueue(new MockResponse()
                .setBody(String.format("nochg %s", ipAddress))
                .addHeader("Content-Type", "text/plain"));

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(new BasicAuthInterceptor("testuser", "testpass"))
                .addInterceptor(new UserAgentInterceptor("TestApp/1.0 test@test.com"))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mockServer.url("/"))
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient)
                .build();

        INoIpApi api = retrofit.create(INoIpApi.class);
        
        retrofit2.Response<String> response = api.update("hostname.example.com", ipAddress).execute();
        String body = response.body();

        Assertions.assertNotNull(body);
        Assertions.assertTrue(body.startsWith("nochg"));
    }

    /**
     * Tests that the API correctly handles a bad authentication response.
     * 
     * Verifies that when invalid credentials are provided, the No-IP API
     * returns "badauth" which should be propagated to the caller.
     */
    @Test
    void shouldHandleBadAuthResponse() throws IOException {
        mockServer.enqueue(new MockResponse()
                .setBody("badauth")
                .addHeader("Content-Type", "text/plain"));

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(new BasicAuthInterceptor("baduser", "badpass"))
                .addInterceptor(new UserAgentInterceptor("TestApp/1.0 test@test.com"))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mockServer.url("/"))
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient)
                .build();

        INoIpApi api = retrofit.create(INoIpApi.class);
        
        retrofit2.Response<String> response = api.update("hostname.example.com", "192.168.1.1").execute();
        String body = response.body();

        Assertions.assertNotNull(body);
        Assertions.assertEquals("badauth", body);
    }

    /**
     * Tests that the API correctly handles a "no host" response.
     * 
     * When the specified hostname does not exist under the account,
     * No-IP returns "nohost". This test verifies error handling.
     */
    @Test
    void shouldHandleNohostResponse() throws IOException {
        mockServer.enqueue(new MockResponse()
                .setBody("nohost")
                .addHeader("Content-Type", "text/plain"));

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(new BasicAuthInterceptor("testuser", "testpass"))
                .addInterceptor(new UserAgentInterceptor("TestApp/1.0 test@test.com"))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mockServer.url("/"))
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient)
                .build();

        INoIpApi api = retrofit.create(INoIpApi.class);
        
        retrofit2.Response<String> response = api.update("nonexistent.example.com", "192.168.1.1").execute();
        String body = response.body();

        Assertions.assertNotNull(body);
        Assertions.assertEquals("nohost", body);
    }

    /**
     * Tests that the API correctly handles an abuse response.
     * 
     * When an account is blocked due to abuse (e.g., violating No-IP terms
     * or sending too many requests), No-IP returns "abuse".
     */
    @Test
    void shouldHandleAbuseResponse() throws IOException {
        mockServer.enqueue(new MockResponse()
                .setBody("abuse")
                .addHeader("Content-Type", "text/plain"));

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(new BasicAuthInterceptor("testuser", "testpass"))
                .addInterceptor(new UserAgentInterceptor("TestApp/1.0 test@test.com"))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mockServer.url("/"))
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient)
                .build();

        INoIpApi api = retrofit.create(INoIpApi.class);
        
        retrofit2.Response<String> response = api.update("hostname.example.com", "192.168.1.1").execute();
        String body = response.body();

        Assertions.assertNotNull(body);
        Assertions.assertEquals("abuse", body);
    }

    /**
     * Tests that the API correctly handles server errors (HTTP 500).
     * 
     * Verifies that when No-IP's servers experience an internal error,
     * the HTTP response code is properly propagated to the caller.
     */
    @Test
    void shouldHandleServerError() throws IOException {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("server error"));

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(new BasicAuthInterceptor("testuser", "testpass"))
                .addInterceptor(new UserAgentInterceptor("TestApp/1.0 test@test.com"))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mockServer.url("/"))
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient)
                .build();

        INoIpApi api = retrofit.create(INoIpApi.class);
        
        retrofit2.Response<String> response = api.update("hostname.example.com", "192.168.1.1").execute();

        Assertions.assertEquals(500, response.code());
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
