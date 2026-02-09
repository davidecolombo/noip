package io.github.davidecolombo.noip.noip;

import java.io.IOException;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Implementation of No-IP API authentication and request handling.
 */
public class NoIpApiImpl {

    /**
     * Create Retrofit instance with authentication interceptors.
     */
    public static retrofit2.Retrofit create(String username, String password, String userAgent) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new BasicAuthInterceptor(username, password));
        httpClient.addInterceptor(new UserAgentInterceptor(userAgent));
        
        return new retrofit2.Retrofit.Builder()
                .baseUrl("https://dynupdate.no-ip.com/")
                .addConverterFactory(retrofit2.converter.scalars.ScalarsConverterFactory.create())
                .client(httpClient.build())
                .build();
    }

    /**
     * Basic authentication interceptor for No-IP API.
     */
    private static class BasicAuthInterceptor implements Interceptor {
        private final String username;
        private final String password;

        public BasicAuthInterceptor(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request.Builder builder = originalRequest.newBuilder()
                    .header("Authorization", 
                            Credentials.basic(username, password));

            Request newRequest = builder.build();
            return chain.proceed(newRequest);
        }
    }

    /**
     * User-Agent interceptor for No-IP API compliance.
     */
    private static class UserAgentInterceptor implements Interceptor {
        private final String userAgent;

        public UserAgentInterceptor(String userAgent) {
            this.userAgent = userAgent;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request.Builder builder = originalRequest.newBuilder()
                    .header("User-Agent", userAgent);

            Request newRequest = builder.build();
            return chain.proceed(newRequest);
        }
    }
}