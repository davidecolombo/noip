package io.github.davidecolombo.noip.noip;

import io.github.davidecolombo.noip.retrofit.BasicAuthInterceptor;
import io.github.davidecolombo.noip.retrofit.UserAgentInterceptor;
import okhttp3.OkHttpClient;
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
}